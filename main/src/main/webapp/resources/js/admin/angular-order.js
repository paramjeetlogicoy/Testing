//Create a random number for version control and avoid caching
var versionCtrl = '?v=' + Math.random(),
_globalPmtModes = [{label:'Cash',value:'Cash'},{label:'Credit Card',value:'Credit Card'},{label:'Paypal',value:'Paypal'},{label:'Split',value:'Split'}];

var ordApp = angular.module(
	'orderApp', [
         'ui.bootstrap',
         'ngSanitize',
         'ordDtlsModule'
     ]
);


/**
 * All the controller functions are implemented here. 
 * Scroll to the bottom to see it being attached to the App.
 */ 
var 
defaultCtrlr = function($scope, $http, $filter, $rootScope, $sanitize, ordDtlService, $interval, $uibModal){
	
	$scope.pageLevelError = '';
	$scope.pg = {};
	$scope.pg.currentPage = 1;
	
	$scope.sortType = 'orderNumber';
	$scope.sortReverse = true;
	$scope.orderSearch = '';
	$scope.orders = '';
	
	$scope.getOrders = function(){
		
		$http.get('/admin/orders/json/', {
			params : { 
				'p' : $scope.pg.currentPage,
				'q' : $scope.orderSearch,
				'o' : ($scope.sortReverse?'-':'') + $scope.sortType
			}

		}).success(function(data){
			if(data.success){
				$scope.orders = data.respData;
				$scope.pg = data.pg
			}
			else if(data.message && data.message!=''){
				$scope.pageLevelError = data.message;
			}
			else{

				$scope.pageLevelError = "There was some error getting the orders."
					+" Please contact G";
			}

		}).error(function(){
			$scope.pageLevelError = "There was some error getting the orders."
					+" Please contact G";
		});	
		
	};
	
	/*Reload the users page every 5 minutes*/
	$scope.reloadOrders = function(){
		if($scope.pg.currentPage == 1) $scope.getOrders();
	};
	
	$scope.pageChanged = function() {
		$scope.getOrders();
	};
	
	$scope.getOrders();
	$interval($scope.reloadOrders, 5 * 60 * 1000);
	
	/**
	 * Order Details Fns
	 **/
	
	$scope.showDetails = function() {
		ordDtlService.orderNumber = this.o.orderNumber;
		
		ordDtlService.nextPrevNumbers = $scope.orders.map(function(x){return x.orderNumber;}); 
		
		ordDtlService.showGallery($rootScope);
	};
	
	
	
	
	/** 
	 * Dispatch Fns 
	 **/
	
	$scope.getSalesDetails = function() {

		if(!this.o.dispatch || this.o.dispatch.salesId==0) return;
		
		var nextPrevDispatches = [];
		$scope.orders.forEach(function(x){
			if(x.dispatch && x.dispatch.salesId!=0) nextPrevDispatches.push(x.dispatch.salesId);
		});
		
		var salesId = this.o.dispatch.salesId,
		orderNumber = this.o.orderNumber;
		
		$http.get('/inventory/apps/listdispatches?id='+salesId).then(function(resp){
			var data = resp.data;
			if(data.success){
			
				var dispatches = data.result,
				
				currentSalesId = salesId,
				dateFinished = dispatches[0].dateFinished,
				clientName = dispatches[0].clientName,
				paymentMode = dispatches[0].paymentMode,
				additionalInfo = dispatches[0].additionalInfo,
				driverName = dispatches[0].driverName,
				tip = dispatches[0].tip,
				splitAmount = dispatches[0].splitAmount,
				commissionPercent = dispatches[0].commissionPercent,
				distInMiles = dispatches[0].distInMiles,
				status = dispatches[0].status;

				
			    var modalInstance = $uibModal.open({
			      templateUrl: '/resources/ng-templates/admin/inv/modals/sales-info.html'+versionCtrl,
			      controller: 'ModalSalesInfoCtrl',
			      backdrop : 'static',
			      size: 'lg',
			      resolve: {
			    	  modalScope: function () {	    		  
			    		  return {
			    			  salesId:currentSalesId,
			    			  dateFinished:dateFinished,
			    			  clientName:clientName,
			    			  paymentMode:paymentMode,
			    			  additionalInfo:additionalInfo,
			    			  driverName:driverName,
			    			  tip:tip,
			    			  splitAmount:splitAmount,
			    			  commissionPercent:commissionPercent,
			    			  source:'dispatch',
			    			  nextPrevDispatches:nextPrevDispatches,
			    			  distInMiles:distInMiles,
			    			  status:status,
			    			  orderNumber:orderNumber
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
	
	$scope.drivers = [];
	$http.get('/inventory/apps/listdrivers').success(function(data){
		if(data.success){
			$scope.drivers = data.result;				
		}
	});
	
	var assignDriverFn = function(order){
		
	    var modalInstance = $uibModal.open({
	      templateUrl: '/resources/ng-templates/admin/inv/modals/assign-driver.html'+versionCtrl,
	      controller: 'ModalAssignDrvInstanceCtrl',
	      backdrop : 'static',
	      resolve: {
	        drivers: function () {
	        	var drivers = $scope.drivers.slice();
	        	drivers.unshift({id:'0',driverName:''});;
	        	return drivers;
	        },
		    order: function () {
	        	return order;
		    }
	      }
	    });
	
	    modalInstance.result.then(
	    	function (ms) {
	    		
	    		$http.post('/admin/order/json/updatedriver', {'driver' : ms.order.dispatch.driverName, 'salesId' : ms.order._id })
	    		.then(function(resp){
	    			
	    			var data = resp.data;
	    			if(data.success) {
	    							
	    				$.post('/inventory/apps/updatedispatch', 
	    						{
	    					id:ms.order.dispatch.salesId,
	    					di:ms.driverId,
	    					mode:'assigndrv', 
	    					opsid:_luvbriteGlobalOpsId
	    					},
	    					function(data){
	    						if(data.success){
	    		    				_lbFns.pSuccess('Order assigned to ' + ms.order.dispatch.driverName);
	    						}
	    						else{
	    							alert(data.message);
	    						}
	    					}
	    				);
	    				
	    			}
	    			else{
	    				
	    				$scope.pageLevelError = data.message;
	    			}				
	    		},
	    		function(){
	    			$scope.errorMsg  = "There was some error updating the driver. "
	    					+"Please contact G.";
	    		});	
	    		
	    	},
	    	
	    	function(){}
	    );	
	};
	
	$scope.assignDriver=function(){
		
		var currentOrder = this.o;
		if(currentOrder.dispatch==null || currentOrder.dispatch.salesId==0){
			
			//getSalesId
			$http.get('/admin/orders/json/getsalesid/'+this.o.orderNumber).then(function(resp){
				var data = resp.data;
				if(data.success){
					currentOrder = data.results[0];	
					
					for(var i=0;i<$scope.orders.length;i++){
						if($scope.orders[i]._id === currentOrder._id){
							$scope.orders[i] = currentOrder;
							break;
						}						
					}
					
					assignDriverFn(currentOrder);
				}
				else{
					$scope.pageLevelError = data.message;
				}
			});
			
		}
		
		else{
			assignDriverFn(currentOrder);
		}
			
	};
	
	var cancelDispatchFn = function(orderId, reason, salesId){
		
	    var modalInstance = $uibModal.open({
	      templateUrl: '/resources/ng-templates/admin/inv/modals/cancel-reason.html'+versionCtrl,
	      controller: 'ModalGenericCtrl',
	      backdrop : 'static',
	      resolve: {
	    	ms: function () {
	        	return {reason:reason, orderId:orderId, salesId:salesId};
	        }
	      }
	    });
	
	    modalInstance.result.then(
	    	function (ms) {
	    		
	    		$http.post('/admin/order/json/savestatus', {'_id' : ms.orderId, 'status' : 'cancelled', 'dispatch' : { 'comments' : ms.reason} })
	    		.then(function(resp){
	    			
	    			var data = resp.data;
	    			if(data.success) {
	    				
	    				for(var i=0;i<$scope.orders.length;i++){
	    					if($scope.orders[i]._id === ms.orderId){						
	    						$scope.orders[i].status = 'cancelled';
	    						$scope.orders[i].dispatch.comments = ms.reason;
	    						break;
	    					}						
	    				}
	    							
	    				$.post('/inventory/apps/updatedispatch', 
	    						{
	    					id:ms.salesId,
	    					reason:ms.reason,
	    					mode:'cancelled', 
	    					opsid:_luvbriteGlobalOpsId
	    					},
	    					function(data){
	    						if(data.success){
	    		    				_lbFns.pSuccess('Order is cancelled. Cancellation notification send to customer');
	    						}
	    						else{
	    							alert(data.message);
	    						}
	    					}
	    				);
	    				
	    			}
	    			else{
	    				
	    				$scope.pageLevelError = data.message;
	    			}				
	    		},
	    		function(){
	    			$scope.errorMsg  = "There was some error updating the status. "
	    					+"Please contact G.";
	    		});	
	    		
	    	},
	    	function(){}
	    );		
	};
	
	$scope.cancelDispatch = function(){
		
		var currentOrder = this.o;
		if(currentOrder.dispatch==null || currentOrder.dispatch.salesId==0){
			
			//getSalesId
			$http.get('/admin/orders/json/getsalesid/'+this.o.orderNumber).then(function(resp){
				var data = resp.data;
				if(data.success){
					currentOrder = data.results[0];	
					
					for(var i=0;i<$scope.orders.length;i++){
						if($scope.orders[i]._id === currentOrder._id){
							$scope.orders[i] = currentOrder;
							break;
						}						
					}
					
					cancelDispatchFn(currentOrder._id, currentOrder.dispatch.comments, currentOrder.dispatch.salesId);
				}
				else{
					$scope.pageLevelError = data.message;
				}
			});
			
		}
		
		else{
			cancelDispatchFn(currentOrder._id, currentOrder.dispatch.comments, currentOrder.dispatch.salesId);
		}
	};
	
	
	var markSoldFn = function(order){
		
	    var modalInstance = $uibModal.open({
		      templateUrl: '/resources/ng-templates/admin/inv/modals/mark-sold.html'+versionCtrl,
		      controller: 'ModalMarkSoldCtrl',
		      backdrop : 'static',
		      resolve: {
		    	  ms: function () {	    		  
		    		  return {itemId:order.dispatch.salesId, tip:0, order:order};
		    	  }
		      }
		    });
		
		    modalInstance.result.then(
		    	function (ms) {	
					
					for(var i=0;i<$scope.orders.length;i++){
						if($scope.orders[i]._id === ms.order._id){
							$scope.orders[i] = currentOrder;
							break;
						}						
					}		    		
		    	},
		    	function(){}
		    );
	};
	
	$scope.markSold = function(){
		
		var currentOrder = this.o;
		if(currentOrder.dispatch==null || currentOrder.dispatch.salesId==0){
			
			//getSalesId
			$http.get('/admin/orders/json/getsalesid/'+this.o.orderNumber).then(function(resp){
				var data = resp.data;
				if(data.success){
					currentOrder = data.results[0];	
					
					for(var i=0;i<$scope.orders.length;i++){
						if($scope.orders[i]._id === currentOrder._id){
							$scope.orders[i] = currentOrder;
							break;
						}						
					}
					
					markSoldFn(currentOrder);
				}
				else{
					$scope.pageLevelError = data.message;
				}
			});
			
		}
		
		else{
			markSoldFn(currentOrder);
		}
	};
};

ordApp

//below fns are defined in angular-general-functions.js
.config(appConfigs)
.run(appRunFns) 
//end fns defined in angular-general-functions.js


.controller('defaultRouteCtrl', defaultCtrlr)

//This is defined in angular-orderdetails-service.js
.controller('ordDtlsCtrlr', ordDtlsCtrlr)
.controller('ModalSalesInfoCtrl', ['$scope', '$uibModalInstance', 'modalScope', '$http', '$filter', '$rootScope', ModalSalesInfoCtrlr])
.controller('ModalAssignDrvInstanceCtrl', ['$scope', '$uibModalInstance', 'drivers', 'order', ModalAssignDrvInstanceCtrlr])
.controller('ModalGenericCtrl', ['$scope', '$uibModalInstance', 'ms', ModalGenericCtrlr])
.controller('ModalMarkSoldCtrl', ['$scope', '$uibModalInstance', 'ms', '$http', '$filter', ModalMarkSoldCtrlr]);

