/**
 * This service is used for orderDetails Popup.
 * Angular module is defined all the way at the bottom
 */

var ordDtlsServiceFn = function($templateRequest, $compile){
	
	var service = {};
	
	service.showGallery = function(scope){
		//Show popup		
		$templateRequest("/resources/ng-templates/admin/ord-dtls-gallery.html?v005")
		.then(function(html){
		      var template = angular.element(html);
		      angular.element('body').addClass('noscroll').append(template);
		      $compile(template)(scope);
		  });
	};
	
	service.selectedFiles = [];
	
	service.orderNumber = 0;
	
	service.nextPrevNumbers = [];
	
	service.cdnPath = _lbGlobalCDNPath; //defined in main.js
	
	service.config = {};
	
	return service;	
},

ordDtlsModule = angular.module('ordDtlsModule', []);
ordDtlsModule.factory('ordDtlService', ordDtlsServiceFn);



var ordDtlsCtrlr = function ($scope, $http, $sanitize, ordDtlService) {

    $scope.hideGallery = function(){
		angular.element('.pop-gallery-overlay').remove();
		angular.element('body').removeClass('noscroll');
	};
	
	$scope.orderNumber = ordDtlService.orderNumber == 0 ? _globalOrderNumber : ordDtlService.orderNumber;
	$scope.nextPrevNumbers = ordDtlService.nextPrevNumbers;
	
	$scope.orderStatuses = ["new", "processing", "cancelled", "delivered", "on-hold"];
	$scope.firstOrder = false;
	$scope.checkIfFirstOrder = function(){
		if($scope.o){
			$scope.firstOrder = false;
			$http.get('/admin/orders/json/firstordercheck/' 
					+ $scope.orderNumber + '/' 
					+ $scope.o.customer._id)
				.then(function(resp){
					if(resp && resp.data){
						if(resp.data.message == 'Y'){
							$scope.firstOrder = true;
						}
					}
				},
				function(){});
		}
	};
	
	$scope.o = {};
	$scope.getDetails = function(){
		
		$scope.errorMsg = "";
		$http.get('/admin/orders/json/' + $scope.orderNumber)
		.success(function(data){
			$scope.o = data;

			$scope.getMemos();
					
			/* Check for the nexPrev order logic */
			prevNextLogic();
			
		}).error(function(){
			$scope.errorMsg = 'There was some error getting the order info. '
				+'Please try later';
		});			
	};
	
	$scope.getDetails();
	
	$scope.statusChange = function(){
		
		$http.post('/admin/order/json/savestatus', {'_id' : $scope.o._id, 'status' : $scope.o.status })
		.success(function(resp){
			if(resp.success) {
				_lbFns.pSuccess('Order status updated to ' + $scope.o.status);
				$scope.reloadMemos();
			}
			else{
				alert(resp.message);
			}				
		})
		.error(function(){
			$scope.errorMsg  = "There was some error saving the status. "
					+"Please contact G.";
		});	
	};
	
	$scope.emailConfirmation = function(){
		
		$http.post('/admin/order/email-confirmation/' + $scope.orderNumber)
		.then(function(resp){
			if(resp && resp.data){
				if(resp.data.success) {
					_lbFns.pSuccess('Email sent.');
					$scope.reloadMemos();
				}
				else{
					$scope.errorMsg  = resp.data.message;
				}	
			}
			else{
				$scope.errorMsg  = "There was some error sending the email. "
						+"Please contact G.";
			}
			
		}, function(){
			$scope.errorMsg  = "There was some error sending the email. "
					+"Please contact G.";
		});	
	};
	
	/* MEMO FNS */
	$scope.memos = [];
	$scope.memopg = {};
	$scope.memopg.currentPage = 1;
	$scope.memoText = "";
	
	$scope.reloadMemos = function(){
		$scope.memopg.currentPage = 1;
		$scope.getMemos();
	};
	
	$scope.getMemos = function(){
		
		$http.get('/admin/logs/orders/' + $scope.o._id, {
			params : {
				'p' : $scope.memopg.currentPage
			}
		})
		.then(function(resp){
			if(resp.data){
				$scope.memos = resp.data.respData;
				$scope.memopg = resp.data.pg;
			}			
		});		
	};
	$scope.saveMemo = function(){
		var log = {
				collection : 'orders',
				key : $scope.o._id,
				details : $('textarea#memoText').val(),
				date : new Date(),
				user : $('#adminHeaderUsername').text()
		}
		
		$http.post('/admin/logs/add', log);
		$scope.memos.unshift(log);
		$('textarea#memoText').val('');
	};	
	/* MEMO FNS ENDS*/
	
	
	
	/* Prev/Next Order Logic */
	$scope.prevOrderNumber = 0;
	$scope.nextOrderNumber = 0;
	
	$scope.getPrevOrder = function(){
		$scope.orderNumber = $scope.prevOrderNumber; 
		$scope.o = {};
		$scope.getDetails();
	};
	
	$scope.getNextOrder = function(){
		$scope.orderNumber = $scope.nextOrderNumber;
		$scope.o = {};
		$scope.getDetails();		
	};
	
	var prevNextLogic = function(){
		$scope.prevOrderNumber = 0;
		$scope.nextOrderNumber = 0;
		
		if(!$scope.nextPrevNumbers) return;
		
		var length = $scope.nextPrevNumbers.length,
		lastIndx = length?length-1:0;
		
		if(lastIndx){
			for(var i=0; i<length; i++){
				if($scope.nextPrevNumbers[i] == $scope.orderNumber){
					if(i !=0 ){
						$scope.prevOrderNumber = $scope.nextPrevNumbers[i-1];
					}
					
					if(i != lastIndx){
						$scope.nextOrderNumber = $scope.nextPrevNumbers[i+1];
					}
					
					break;
				}
			}
		}
	};

};