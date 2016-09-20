/**
 * 
 */
var addPacketCtrlr = function($scope, $http, $routeParams, driverMode, $uibModal, $rootScope, currentUser){
	
	$rootScope.rootPage = "Packets";

	$scope.controlId = currentUser.ctrlid;
	
	if($scope.controlId>100) return;
	
	$scope.alertTxt = '';	
	$scope.packetBtnText = 'ADD';
	$scope.currentPacketId = 0;
	$scope.purchaseId = 0;
	$scope.productName = "";
	$scope.purchaseSpecific = false;
	
	$scope.totalItems = 14;
	$scope.currentPage = 1;
	$scope.itemsPerPage = 4;
	
	if($routeParams && $routeParams.p){
		$scope.purchaseId = $routeParams.p;
		$scope.purchaseSpecific = true;
		$('.packetID').focus();
	}	


	/**
	 * If drivers login, then driverMode is true. We have to set the shopSelected1 to
	 * the first shop.
	 **/
	$scope.driverMode = driverMode;	
	$scope.shopSelected1 = {id:'-1',shopName:'All Shops'};
	
	$scope.shops = [];
	$scope.shops1 = [];
	$http.get('/inventory/apps/listshops').success(function(data){
		if(data.success){
			$scope.shops = data.result;
	
			$scope.shops1 = data.result.slice(); //Get a copy of the array

			if($scope.driverMode){
				$scope.shopSelected1 = $scope.shops1[0];
			}
			else{
				$scope.shops1.unshift($scope.shopSelected1);
			}
		}
	});
	
	$scope.items = [{value:'all',label:'All Units'},
	                {value:'ns',label:'Not Sold'},{value:'sold',label:'Sold'}];
	$scope.itemSelected = $scope.items[0];
		
	
	$scope.sku = '';
	
	$scope.addPacket=function(){
		
		var currPacketId = $scope.currentPacketId,
		currPacketCode = $scope.packetCode,
		currWeightInGrams = $scope.weightInGrams,
		currMarkedPrice = $scope.markedPrice;
		
		if($scope.currentPacketId!=0){						
			$.post('/inventory/apps/updatepacket', {mode:'basic', 
						id:$scope.currentPacketId, pc:$scope.packetCode,
						wt:$scope.weightInGrams, price:$scope.markedPrice, 
						opsid:_luvbriteGlobalOpsId},
				
				function(data){
					if(data.success){ 
						for(var i=0;i<$scope.packets.length;i++){
							if($scope.packets[i].id==currPacketId){	
								$scope.packets[i].packetCode = currPacketCode;
								$scope.packets[i].weightInGrams = currWeightInGrams;
								$scope.packets[i].markedPrice = currMarkedPrice;

								break;
							}
						}

						$scope.currentPacketId = 0;
						$scope.weightInGrams = '';
						$scope.markedPrice = '';

						$scope.packetCode = '';
						$scope.packetBtnText = 'ADD';
					}
					else 
						$scope.alertTxt = data.message;
					
					try{$scope.$apply();}catch(err){}
					
					if($scope.purchaseSpecific) $('.packetID').focus();
					
					/**Refresh the purchase order details */
					$scope.refreshPurchaseInfo();
				}
			);			
		}
		else {	
			
			var packetPresent = false,
			currPacketCode = $scope.packetCode;
			for(var i=0;i<$scope.packets.length;i++){
				if($scope.packets[i].packetCode==$scope.packetCode){
					packetPresent = true;
					break;
				}
			}
			
			if(packetPresent){
				$scope.alertTxt = 'Packet already scanned!';
				return;	
			}
			
			$scope.packets.push({
				productName : $scope.productName,
				
				packetCode : $scope.packetCode, 
				weightInGrams : $scope.weightInGrams,
				markedPrice : $scope.markedPrice,
				returnReason : '',
				returnDetailsId : 0
			});
			
			$.post('/inventory/apps/newpacket',{pi:$scope.purchaseId, wt:$scope.weightInGrams,
						price:$scope.markedPrice, pc:$scope.packetCode,
						opsid:_luvbriteGlobalOpsId},
						
				function(data){
					if(data.success){
						for(var i=0;i<$scope.packets.length;i++){
							if($scope.packets[i].packetCode==data.packet.packetCode){
								$scope.packets[i].dateAdded = data.packet.dateAdded;
								$scope.packets[i].id = data.packet.id;
								try{$scope.$apply();}catch(err){}
								break;
							}
						}

						$scope.currentPacketId = 0;
						$scope.packetCode = '';
						
						/** We want to keep the previous weight and unitprice for easy entry**/
						//$scope.weightInGrams = '';
						//$scope.markedPrice = '';
						
						$('.packetID').focus();
						
						/**Refresh the purchase order details */
						$scope.refreshPurchaseInfo();
					}
					else {						
						$scope.alertTxt = data.message;
						for(var i=0;i<$scope.packets.length;i++){
							if($scope.packets[i].packetCode==currPacketCode){
								$scope.packets.splice(i,1);
								try{$scope.$apply();}catch(err){}
								break;
							}
						}
					}
				}
			);			
		}
		
	};
	
	$scope.deletePacket=function(){		
		var currentPacketId = this.packet.id;
		if(confirm('Are you sure you want to delete this item?')){			
			$.post('/inventory/apps/updatepacket', {mode:'delete', 
						id:currentPacketId, opsid:_luvbriteGlobalOpsId},
				function(data){
					if(data.success){
						for(var i=0;i<$scope.packets.length;i++){
							if($scope.packets[i].id==data.packet.id){
								$scope.packets.splice(i, 1);
								try{$scope.$apply();}catch(err){}
								break;
							}
						}
					}
					else alert(data.message);
				}
			);	
		}
	};
	
	$scope.editPacket=function(){
		
		$scope.currentPacketId = this.packet.id;
		$scope.packetCode = this.packet.packetCode;
		$scope.weightInGrams = this.packet.weightInGrams;
		$scope.markedPrice = this.packet.markedPrice;
		
		$scope.packetBtnText = 'SAVE';
		
		/**
		 * Scroll up to the form and show subtle color change.
		 **/
		_lbFns.showViewForm();
	};
	
	$scope.cancelEdit=function($event){
		$event.preventDefault();
		
		$scope.currentPacketId = 0;
		$scope.packetCode = '';
		$scope.weightInGrams = '';
		$scope.markedPrice = '';
		
		$scope.packetBtnText = 'ADD';		
	};
	
	$scope.assignShop=function(){
	
		$scope.currentPacketId = this.packet.id;
		$scope.currentshopId = this.packet.shopId;
		
	    var modalInstance = $uibModal.open({
	      templateUrl: '/resources/ng-templates/admin/inv/modals/assign-shop.html'+versionCtrl,
	      controller: 'ModalAssignShopInstanceCtrl',
	      backdrop : 'static',
	      resolve: {
	        shops: function () {
	        	return $scope.shops;
	        },
	        shopId: function () {
	        	return $scope.currentshopId;
		    },
		    itemId: function () {
	        	return $scope.currentPacketId;
		    }
	      }
	    });
	
	    modalInstance.result.then(
	    	function (modalScope) {
				for(var i=0;i<$scope.packets.length;i++){
					if($scope.packets[i].id==modalScope.itemId){
						$scope.packets[i].shopName = modalScope.shopSelected.shopName;
						$scope.packets[i].shopId = modalScope.shopSelected.id;
						break;
					}
				}
				
				$.post('/inventory/apps/updatepacket', {id:modalScope.itemId, sp:modalScope.shopSelected.id,
							mode:'assignshop', opsid:_luvbriteGlobalOpsId},
					function(data){
						if(!data.success) alert(data.message);
					}
				);
	    		
	    	},
	    	function(){
	    		$scope.currentPacketId = 0;
	    	}
	    );		
	};
	
	$scope.getSalesDetails=function(){

		if(this.packet.salesId==0) return;
		
		$scope.currentSalesId = this.packet.salesId;
		
		
		$http.get('/inventory/apps/listdispatches?id='+$scope.currentSalesId).success(function(data){
			if(data.success){
				$scope.dispatches = data.result;
				$scope.dateFinished = $scope.dispatches[0].dateFinished;
				$scope.dateArrived = $scope.dispatches[0].dateArrived;
				$scope.clientName = $scope.dispatches[0].clientName;
				$scope.paymentMode = $scope.dispatches[0].paymentMode;
				$scope.additionalInfo = $scope.dispatches[0].additionalInfo;
				
			    var modalInstance = $uibModal.open({
			      templateUrl: '/resources/ng-templates/admin/inv/modals/sales-info.html'+versionCtrl,
			      controller: 'ModalSalesInfoCtrl',
			      backdrop : 'static',
			      size: 'lg',
			      resolve: {
			    	  modalScope: function () {	    		  
			    		  return {
			    			  salesId:$scope.currentSalesId,
			    			  dateFinished:$scope.dateFinished,
			    			  dateArrived:$scope.dateArrived,
			    			  clientName:$scope.clientName,
			    			  paymentMode:$scope.paymentMode,
			    			  additionalInfo:$scope.additionalInfo,
			    			  driverName:$scope.driverName,
			    			  tip:$scope.dispatches[0].tip,
			    			  splitAmount:$scope.dispatches[0].splitAmount,
			    			  commissionPercent:$scope.dispatches[0].commissionPercent,
			    			  distInMiles:$scope.dispatches[0].distInMiles,
			    			  status:$scope.dispatches[0].status,
			    			  source:'packet'
			    		  };
			    	  }
			      }
			    });
			
			    modalInstance.result.then(
			    	function (){},
			    	function (){}
			    );
			}
		});		
	};
	
	
	$('.packets-section').on('mouseenter','tr.returned',function(){
		var $tr = $(this);
		$tr.removeClass('returned').after('<tr class="danger"><td colspan="' + $tr.find('td').size() 
				+ '"><strong class="marg-l2">This packet was returned. Reason - ' + $tr.attr('title') 
				+ '<strong></td></tr>')
	});
	
	
	/**Pagination Logic**/
	$scope.pg = {"currentPage":1,"endCount":0,"itemsPerPage":0,"startCount":0,"totalItems":0};	
	
	$scope.sortBy = '';
	$scope.sdir = 'ASC'	
	$scope.pageChanged=function(){
		$scope.reload();
	};
	/***/
	
	$scope.sortCol=function(param, e){
		//generic fn defined in main.js
		sortColFns($scope, param, e);
	};

	
	$scope.pd = {};
	$scope.refreshPurchaseInfo = function(){
		if($scope.purchaseId!=0){
			$http.get('/inventory/apps/purchaseinfo?pid='+$scope.purchaseId).success(function(data){
				if(data.success){
					$scope.pd = data.result;
				}
			});
		}
	};
	$scope.refreshPurchaseInfo();
	
	/*Bulk Update*/
	$scope.showBulkForm = false;
	$scope.bulkPrice = 0;
	$scope.bulkWt = 0;	
	$scope.bulkUpdate = function(){
		if($scope.bulkPrice != 0 && $scope.bulkWt != 0){			
			$.post('/inventory/apps/updatepacket', {'p':$scope.bulkPrice, 'w':$scope.bulkWt, 
				'pi':$scope.purchaseId, 'mode':'bulkUpdate', 
				'opsid':_luvbriteGlobalOpsId},
				function(data){
					if(data.success){
						alert('Bulk update Success');
						$scope.reload();
					}
					else{
						alert(data.message);
					}
				}
			);
		}
	};
	/*Bulk Update Ends*/
	
	
	/*Bulk Add*/
	$scope.resetBuldAdd = function(){
		$scope.bulkAddCount = 0;
		$scope.bulkAddPrice = 0;
		$scope.bulkAddWt = 0;	
	};
	$scope.resetBuldAdd();
	$scope.bulkAdd = function(){
		if($scope.bulkAddCount > 0 
				&& $scope.bulkAddPrice > 0 
				&& $scope.bulkAddWt > 0){
			
			$.post('/inventory/apps/bulkpacketadd', {
				'p':$scope.bulkAddPrice, 
				'w':$scope.bulkAddWt, 
				'c':$scope.bulkAddCount, 
				'pi':$scope.purchaseId, 
				'opsid':_luvbriteGlobalOpsId
				},
				function(data){
					if(data.success){
						$scope.reload();
						$scope.resetBuldAdd();
						
						//create new array and add productname ~ barcode to it
						var bcArray = [],
						codes = data.packetCodes.split(',');
						if(codes && codes.length){
							var pname = $scope.pd.productName;
							if(pname.length > 20){
								pname = pname.substr(0,15) + "..." 
									+ pname.substr(pname.length-4, pname.length);
							}
							
							
							codes.forEach(function(x){
								bcArray.push(x + '~' + pname);
							});
							
							$rootScope.addToBCQueue(bcArray);
						}
					}
					else{
						alert(data.message);
					}
				}
			);
		}
	};
	/*Bulk Add Ends*/
	
	$scope.getLabel = function(){
		
		//create new array and add productname ~ barcode to it
		var bcArray = [];
		if(this.packet.packetCode){
			var pname = $scope.pd.productName;
			if(pname.length > 20){
				pname = pname.substr(0,15) + "..." 
					+ pname.substr(pname.length-4, pname.length);
			}
			
			bcArray.push(this.packet.packetCode + '~' + pname);
			
			$rootScope.addToBCQueue(bcArray);
		}
	};

	$scope.packets = [];
	$scope.reload=function(){
		$scope.alertTxt = '';
		
		var urlParams = "?cpage="+$scope.pg.currentPage
				+ "&sort="+$scope.sortBy
				+ "&sdir="+$scope.sdir
				+ "&p="+$scope.purchaseId
				+ "&pc="+$scope.sku
				+ "&"+$scope.itemSelected.value+"=true"
				+ "&sp="+$scope.shopSelected1.id;
		
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

	$scope.reload();
};