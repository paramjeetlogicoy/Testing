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
	
	$scope.showDetails = function(){
		ordDtlService.orderNumber = this.o.orderNumber;
		
		ordDtlService.nextPrevNumbers = $scope.orders.map(function(x){return x.orderNumber;}); 
		
		ordDtlService.showGallery($rootScope);
	};
	
	
	
	
	/** Dispatch Fns **/

	
	$scope.getSalesDetails=function(){

		if(!this.o.dispatch || this.o.dispatch.salesId==0) return;
		
		$rootScope.globals = { currentUser : null};
		
		var nextPrevDispatches = [];
		$scope.orders.forEach(function(x){
			if(x.dispatch && x.dispatch.salesId!=0) nextPrevDispatches.push(x.dispatch.salesId);
		});
		
		var salesId = this.o.dispatch.salesId;
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
			    			  status:status
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
	    	function (modalScope) {
				
				//update mongoDB order and then update postgres				
/*				$.post('/inventory/apps/updatedispatch', {id:modalScope.itemId, di:modalScope.driverSelected.id,
							mode:'assigndrv', opsid:_luvbriteGlobalOpsId},
					function(data){
						if(!data.success) alert(data.message);
					}
				);*/
	    		
	    	},
	    	function(){
	    	}
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
					assignDriverFn(currentOrder);
				}
			});
			
		}
		
		else{
			assignDriverFn(currentOrder);
		}
			
	};
	
	
	$scope.cancelDispatch=function($event){
	
		var currentDispatchId = this.dispatch.id;
		var cancellationReason = this.dispatch.cancellationReason;
		
	    var modalInstance = $uibModal.open({
	      templateUrl: '/resources/ng-templates/admin/inv/modals/cancel-reason.html'+versionCtrl,
	      controller: 'ModalGenericCtrl',
	      backdrop : 'static',
	      resolve: {
	    	modalScope: function () {
	        	return {reason:cancellationReason, itemId:currentDispatchId};
	        }
	      }
	    });
	
	    modalInstance.result.then(
	    	function (modalScope) {
				for(var i=0;i<$scope.dispatches.length;i++){
					if($scope.dispatches[i].id==modalScope.itemId){
						$scope.dispatches[i].cancellationReason = modalScope.reason;
						$scope.dispatches[i].paymentMode = 'Cancelled - '+modalScope.reason;
						break;
					}
				}
				
				$.post('apps/updatedispatch', {id:modalScope.itemId, reason:modalScope.reason,
							mode:'cancelled', opsid:_luvbriteGlobalOpsId},
					function(data){
						if(!data.success) alert(data.message);
					}
				);
	    		$scope.currentDispatchId = 0;
	    		
	    	},
	    	function(){
	    		$scope.currentDispatchId = 0;
	    	}
	    );		
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
.controller('ModalAssignDrvInstanceCtrl', ['$scope', '$uibModalInstance', 'drivers', 'order', ModalAssignDrvInstanceCtrlr]);

