//Create a random number for version control and avoid caching
var versionCtrl = '?v=' + Math.random();

var cpnApp = angular.module(
	'couponsApps', [
         'ngSanitize',
         'ui.bootstrap'
     ]
);


/**
 * All the controller functions are implemented here. 
 * Scroll to the bottom to see it being attached to the App.
 */ 
var 
defaultCtrlr = function($scope, $http, $filter, $sanitize, $templateRequest, $compile){
	
	$scope.pageLevelError = '';
	$scope.pg = {};
	$scope.pg.currentPage = 1;
	$scope.coupons = [];
	
	$scope.sortType = '_id';
	$scope.sortReverse = true;
	$scope.couponSearch = '';
	
	$scope.getCoupons = function(){
		
		$http.get('/admin/coupons/json/', {
			params : { 
				'p' : $scope.pg.currentPage,
				'q' : $scope.couponSearch,
				'o' : ($scope.sortReverse?'-':'') + $scope.sortType
			}

		}).success(function(data){
			if(data.success){
				$scope.coupons = data.respData;
				$scope.pg = data.pg
			}
			else if(data.message && data.message!=''){
				$scope.pageLevelError = data.message;
			}
			else{

				$scope.pageLevelError = "There was some error getting coupons."
					+" Please contact G";
			}

		}).error(function(){
			$scope.pageLevelError = "There was some error getting coupons."
					+" Please contact G";
		});	
		
	};
	
	$scope.pageChanged = function() {
		$scope.getCoupons();
	};
	
	$scope.getCoupons();
	
	$scope.couponPopUp = function(){
		
		//load the products list if not present
		if(!$scope.products){
			$http.get('/admin/products/json/all')
			.success(function(data){
				
				$scope.products = data;
				
				$scope.showPopup();
				
			})
			.error(function(){
				alert('There was some error getting the product list. Please contact G');
			});
			
		}
		else{
			
			$scope.showPopup();
		}
	};
	
	
	
	$scope.showPopup = function(){
		
		//Show popup		
		$templateRequest("/resources/ng-templates/admin/coupon-modal.html")
		.then(function(html){
		      var template = angular.element(html);
		      angular.element('body').addClass('noscroll').append(template);
		      $compile(template)($scope);
		 });
		
	};
	
	
	$scope.closeModal = function(){
		angular.element('.pop-gallery-overlay').remove();
		angular.element('body').removeClass('noscroll');
	};
	
	
	
	/**
	 * Add/Edit Coupon
	 **/

	$scope.couponTypes = ['F', 'R'];
	$scope.couponStatus = [true, false];
	
	$scope.newCoupon = function(){
		$scope.today = new Date();
		$scope.cp = {'type':'R', 'active': true};
		$scope.couponCount = 0;	
	    $scope.prodSelected = undefined;
	    $scope.mode = 'new';
	    $scope.bulkCouponMsg = '';
		
		$scope.couponPopUp();
	};

	
	$scope.editCoupon = function(){
	    $scope.mode = 'edit';
	    $scope.bulkCouponMsg = '';
		$scope.couponCount = 1;
		$scope.cp = this.c;
		
		if($scope.cp.expiry)
			$scope.expiryDate = new Date($scope.cp.expiry);
		
		$scope.couponPopUp();
	};
	
	
	$scope.expiryDate = '';
	$scope.updateExpiryDate = function(){		
		$scope.cp.expiry = $scope.expiryDate ? $scope.expiryDate.getTime() : '';  
	};
	
	
	$scope.getProductName = function(pid){
		var name = "";
		for(var i=0; i<$scope.products.length; i++){
			if($scope.products[i]._id == pid){
				name = $scope.products[i].name;
				break;
			}
		}
		return name;
	};
	
	$scope.addProduct = function($item){
		if(!$scope.cp.pids) 
			$scope.cp.pids = [];
		
		$scope.cp.pids.push($item._id);
		$scope.prodSelected = "";
	};
	
	$scope.removeProduct = function(){
		for(var i=0; i<$scope.cp.pids.length; i++){
			if($scope.cp.pids[i] == this.pid){
				$scope.cp.pids.splice(i,1);
				break;
			}
		}
	};

	$scope.checkCoupon = function(){
		$scope.couponExists = false;				
		$http.get('/admin/coupons/json/check/?c=' + $scope.cp._id)
		.success(function(data){
			if(data){
				$scope.couponExists = true;
			}
		});
	};
	
	$scope.createCoupon = function(){

		var url = '/save';
		if($scope.couponCount>1){
			url = '/bulk/' + $scope.couponCount;
		}
		
		$http.post('/admin/coupons/json' + url, $scope.cp)
		.success(function(resp){
			
			if($scope.couponCount>1){
				_lbFns.pSuccess('Coupons created');
				$scope.bulkCouponMsg = resp.message;
			}
			else{
				
				if($scope.mode == 'new') {
					_lbFns.pSuccess('Coupon created - ' + $scope.cp._id);
				}
				else {
					_lbFns.pSuccess('Coupon updated');
				}	
				
				//Close the modal
				$scope.closeModal();			
			}
			
			
			//Refresh the coupon list
			$scope.getCoupons();
		})
		
		.error(function(){
			$scope.errorMsg  = "There was some error saving the coupon. "
					+"Please contact G.";
		});	
	};
    
};

cpnApp
//below fns are defined in angular-general-functions.js
.config(appConfigs)
.run(appRunFns) 
//end fns defined in angular-general-functions.js


.controller('defaultRouteCtrl', defaultCtrlr);
