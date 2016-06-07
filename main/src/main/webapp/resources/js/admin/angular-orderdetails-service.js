/**
 * This service is used for orderDetails Popup.
 * Angular module is defined all the way at the bottom
 */

var ordDtlsServiceFn = function($templateRequest, $compile){
	
	var service = {};
	
	service.showGallery = function(scope){
		//Show popup		
		$templateRequest("/resources/ng-templates/admin/ord-dtls-gallery.html")
		.then(function(html){
		      var template = angular.element(html);
		      angular.element('body').addClass('noscroll').append(template);
		      $compile(template)(scope);
		  });
	};
	
	service.selectedFiles = [];
	
	service.orderNumber = 0;
	
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
	
	$scope.orderStatuses = ["new", "processing", "cancelled", "delivered", "on-hold"];
	
	$scope.o = {};
	$scope.getDetails = function(){
		
		$scope.errorMsg = "";
		$http.get('/admin/orders/json/' + $scope.orderNumber)
		.success(function(data){
			$scope.o = data;			
			
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
	}

};