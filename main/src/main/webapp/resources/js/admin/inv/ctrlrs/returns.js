var returnsCtrlr = function($scope, $http, $rootScope, currentUser){

	$rootScope.rootPage = "Returns";
	$scope.controlId = currentUser.ctrlid;
	
	if($scope.controlId>100) return;	
	
	$scope.packetCode = '';	
	$scope.reason = '';
	$scope.reasons = ['Customer didn\'t like the item'];
	$scope.btnTxt = 'ADD';

	$http.get('/inventory/apps/listreasons').success(function(data){
		if(data.success && data.result.length > 0){
			$scope.reasons = data.result;
		}
	});	
	
	
	$scope.removeReturn=function(){		
		
		var returnId = this.return.id;
		if(returnId !== undefined && returnId != 0){
			
			$http.get("apps/removereturn",{
				params: {
					id:returnId, 
					opsid:_luvbriteGlobalOpsId
				}
			})
			.success(function(res){
				
				if(res.success){
					for(var i=0;i<$scope.returns.length;i++){
						if($scope.returns[i].id==returnId){
							$scope.returns.splice(i, 1);
							break;
						}
					}
				}
				else{
					alert(res.message);
				}
			}).error(function(){
				alert("There was some error removing the return.");
			});
		}
		else{
			alert('Error deleting the return. Please contact G.');
		}
	};
	
	
	$scope.addReturn=function(){				
		var proceed = true;
		for(var i=0;i<$scope.returns.length;i++){
			if($scope.returns[i].packetCode==$scope.packetCode){
				alert('This packet has been already scanned');
				proceed = false;
				break;
			}
		}
		
		if(!proceed) return;
		
		$scope.returns.push({
			packetCode:$scope.packetCode,
			reason:$scope.reason,
			productName:''
		});

		$scope.btnTxt = 'Adding...';
		$.post('/inventory/apps/newreturn', {pc:$scope.packetCode, reason:$scope.reason, opsid:_luvbriteGlobalOpsId},
			function(data){
				$scope.btnTxt = 'ADD';
				if(data.success){
					$scope.packetCode = '';
					
					$('#packetCode').focus();
					
					for(var i=0;i<$scope.returns.length;i++){
						if($scope.returns[i].packetCode==data.returns.packetCode){
							$scope.returns[i].productName = data.returns.productName;
							break;
						}
					}
				}else{
					alert(data.message);
				}
				
				try{$scope.$apply();}catch(err){}
			}
		);
	};
	
	/**Pagination Logic**/
	$scope.pg = {"currentPage":1,"endCount":0,"itemsPerPage":0,"startCount":0,"totalItems":0};	
	
	$scope.sortBy = '';
	$scope.sdir = 'ASC'	
	$scope.pageChanged=function(){
		$scope.reload();
	};
	
	$scope.returns = [];
	$scope.reload=function(){		
		var urlParams = "?cpage="+$scope.pg.currentPage;
		
		$http.get('/inventory/apps/listreturns'+urlParams).success(function(data){
			if(data.success){
				$scope.returns = data.result;
				if(data.pg){
					$scope.pg = data.pg;
				}
			}
		});	
	};	
	
	//Initial load
	$scope.reload();
};