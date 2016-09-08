var cusApp = angular.module(
	'customerApp', [
	     'ngRoute',
         'ngSanitize',
         'ui.bootstrap'
     ]
);


/**
 * All the controller functions are implemented here. 
 * Scroll to the bottom to see it being attached to the App.
 */ 
var 
defaultCtrlr = function($scope, $http){
	
	$scope.user = {};

	$scope.loadUser = function(){

		
		$http.get('/customer/json/' + _globalUserId)
		.success(function(data){
			if(data){
				$scope.user = data;
			}
			else {

				$scope.pageLevelError = "There was some error getting the information."
					+" Please contact support.";
			}

		}).error(function(){

			$scope.pageLevelError = "There was some error getting the information."
				+" Please contact support.";
		});
		
	};
	
	$scope.loadUser();
	$('.nav-orders').removeClass('active');
	$('.nav-profile').addClass('active');
	
	
	//Due to thymeleaf HTML validation, I had to put these validations here.	
	$scope.pwd1Validation = function(){
		return ($scope.userPassword.password1.$invalid 
				&& !$scope.userPassword.password1.$pristine);
	};	
	$scope.pwd2Validation1 = function(){
		return (($scope.userPassword.password2.$invalid || ($scope.password2 != $scope.password1)) 
				&& !$scope.userPassword.password2.$pristine);
	};	
	$scope.pwd2Validation2 = function(){
		return ($scope.userPassword.password2.$invalid 
				&& !$scope.userPassword.password2.$pristine);
	};	
	

	$scope.changePassword = function(){
		$scope.password1 = '';
		$scope.password2 = '';
		
		$('.customer-passwordchange').slideDown();
		
	};
	
	$scope.hidePasswordForm = function(){
		$('.customer-passwordchange').slideUp();
	};
	
	
	$scope.savePassword = function(){
		
		$http.post('/customer/savep', 
				{'_id' : $scope.user._id, 'password' : $scope.password1 })
		.success(function(resp){
			if(resp.success) {
				_lbFns.pSuccess('Password Updated');
				$scope.hidePasswordForm();
			}
			else{
				alert(resp.message);
			}
				
		})
		.error(function(){
			$scope.errorMsg  = "There was some error saving the password. "
					+"Please contact G.";
		});	
	};
	
	$scope.editUserAddress = function(){
		$('.user-billing-info').slideUp();
		$('.user-billing-form').slideDown();
	};
	
	$scope.addUserAddress = function(){
		$scope.user.billing = {'firstName':$scope.user.fname,'lastName':$scope.user.lname};
		$('.user-billing-info').slideUp();
		$('.user-billing-form').slideDown();
	};
	
	$scope.saveUserAddress = function(){

		
		if($scope.user._id && $scope.userAddress.$valid && $scope.userAddress.$dirty){
			
			$http.post('/customer/' + $scope.user._id+ '/saveaddr', $scope.user.billing)
			.then(
					function(resp){
						if(resp.data && resp.data.success){
							_lbFns.pSuccess('Address Saved.');
							
							$('.user-billing-info').slideDown();
							$('.user-billing-form').slideUp();
						}
					
						else if(resp.data && resp.data.message){
							$scope.pageLevelAlert = resp.data.message;
						}
						else{
							$scope.pageLevelAlert = $rootScope.errMsgPageRefresh;
						}
					},
					
					function(){
						$scope.pageLevelAlert = $rootScope.errMsgPageRefresh;
					}
			);
			
		}
	};
},

orderCtrlr = function($scope, $http){

	$scope.pageLevelError = '';
	$scope.pg = {};
	$scope.pg.currentPage = 1;
	
	$scope.sortType = '_id';
	$scope.sortReverse = true;
	$scope.orderSearch = '';
	
	$scope.orders = [];

	$scope.getOrders = function(){
		
		$http.get('/customer/json/' + _globalUserId + '/orders', {
			params : { 
				'p' : $scope.pg.currentPage,
				'q' : $scope.orderSearch,
				'o' : ($scope.sortReverse?'-':'') + $scope.sortType
			}
		})
		.success(function(data){
			if(data.success){
				$scope.orders = data.respData;
				$scope.pg = data.pg;
			}
			else if(data.message && data.message!==''){
				$scope.pageLevelError = data.message;
			}
			else{

				$scope.pageLevelError = "There was some error getting orders."
					+" Please contact support.";
			}

		}).error(function(){

			$scope.pageLevelError = "There was some error getting orders."
				+" Please contact support.";
		});
		
	};
	
	$scope.pageChanged = function() {
		$scope.getOrders();
	};
	
	$scope.getOrders();
	
	$('.nav-profile').removeClass('active');
	$('.nav-orders').addClass('active');
},

orderDetailsCtrl = function($scope, $http, $routeParams){
	
	$scope.orderNumber = $routeParams.orderNumber;

	$scope.getDetails = function(){
		
		$scope.errorMsg = "";
		$http.get('/customer/json/orders/' + $scope.orderNumber)
		.success(function(data){
			$scope.o = data;			
			
		}).error(function(){
			$scope.errorMsg = 'There was some error getting the order info. '
				+'Please try later';
		});			
	};
	
	$scope.getDetails();
	
	$('.nav-profile, .nav-orders').removeClass('active');
};

cusApp
//below fns are defined in angular-general-functions.js
.config(appConfigs)
.run(appRunFns) 
//end fns defined in angular-general-functions.js

.config(['$routeProvider',
    function($routeProvider) {
      $routeProvider.
	    when('/profile', {
	         templateUrl: 'profilePage',
	         controller: 'defaultRouteCtrl'
	    }).
	    when('/orders', {
	         templateUrl: 'ordersPage',
	         controller: 'orderCtrl'
	    }).
	    when('/details/:orderNumber', {
	         templateUrl: 'ordersDetailsPage',
	         controller: 'orderDetailsCtrl'
	    }).
	    otherwise({ redirectTo: "/profile" });
}])

.controller('defaultRouteCtrl', defaultCtrlr)
.controller('orderCtrl', orderCtrlr)
.controller('orderDetailsCtrl', orderDetailsCtrl);