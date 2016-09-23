var bulkAssignCtrlr = function($scope, $http, $rootScope){

	$rootScope.rootPage = "Bulk Assign";
	$scope.controlId = _luvbriteGlobalOpsId;
	
	if($scope.controlId>100) return;	
	
	$scope.packetCode = '';	
	$scope.btnTxt = 'ADD';
	$scope.newpackets = [];
	$scope.sortCtrl = 100;
	$scope.shopSelected={};
	$http.get('/inventory/apps/listshops').success(function(data){
		if(data.success){
			$scope.shops = data.result;
		}
	});
	
	$scope.addPacket=function(){				
		var proceed = true;
		for(var i=0;i<$scope.newpackets.length;i++){
			if($scope.newpackets[i].packetCode==$scope.packetCode){
				alert('This packet has been already scanned');
				proceed = false;
				break;
			}
		}
		
		if(!proceed) return;
		
		$scope.newpackets.push({
			packetCode:$scope.packetCode,
			productName:'',
			sort: $scope.sortCtrl++
		});

		$scope.btnTxt = 'Adding...';
		$.post('/inventory/apps/updatepacket', {pc:$scope.packetCode, sp:$scope.shopSelected.id,
					mode:'assignshop', opsid:_luvbriteGlobalOpsId},
			function(data){
				$scope.btnTxt = 'ADD';
				if(data.success){
					$scope.packetCode = '';
					$('#packetCode').focus();
					for(var i=0;i<$scope.newpackets.length;i++){
						if($scope.newpackets[i].packetCode==data.packet.packetCode){
							$scope.newpackets[i].productName = data.packet.productName;
							try{$scope.$apply();}catch(err){}
							break;
						}
					}
				}else{
					alert(data.message);
				}
			}
		);
	};
	
	$scope.shopFilter=function(){
		$scope.reload();
		$scope.newpackets = [];
	};
	
	/**Pagination Logic**/
	$scope.pg = {"currentPage":1,"endCount":0,"itemsPerPage":0,"startCount":0,"totalItems":0};	
	
	$scope.sortBy = '';
	$scope.sdir = 'ASC'	
	$scope.pageChanged=function(){
		$scope.reload();
	};
	
	$scope.packets = [];
	$scope.reload=function(){		
		var urlParams = "?cpage="+$scope.pg.currentPage
				+ "&sp="+$scope.shopSelected.id;
		
		$http.get('/inventory/apps/listpackets'+urlParams).success(function(data){
			if(data.success){
				$scope.packets = data.result;
				if(data.pg){
					$scope.pg = data.pg;
				}
				
				if($scope.purchaseSpecific && $scope.packets.length>0){
					$scope.productName = $scope.packets[0].productName;
				}
			}
		});	
	};	
};