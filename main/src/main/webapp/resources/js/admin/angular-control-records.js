//Create a random number for version control and avoid caching
var versionCtrl = '?v=' + Math.random();

var crApp = angular.module(
	'controlRecordApp', [
         'ngSanitize',
         'ui.bootstrap'
     ]
);


/**
 * All the controller functions are implemented here. 
 * Scroll to the bottom to see it being attached to the App.
 */ 
var 
defaultCtrlr = function($scope, $http, $filter){
	
	$scope.pageLevelError = '';
	$scope.records = [];
	
	$scope.getRecords = function(){
		
		$http.get('/admin/ctrls/json/all')
		.then(function(resp){			
			$scope.records = resp.data;
		});	
		
	};	
	$scope.getRecords();
	
	$scope.reloadConfig = function(){
		
		$http.post('/admin/ctrls/reloadconfig')
		.then(function(resp){
			var data = resp.data;
			if(data.success){
				_lbFns.pSuccess('Config Reloaded');
			}
			else{
				alert(data.message);
			}
		});
	};
	
	
	$scope.saveCtrlRecord = function(){
		$http.post('/admin/ctrls/saverecord', this.r)
		.then(function(resp){
			var data = resp.data;
			if(data.success){
				_lbFns.pSuccess('Record Saved');
				$scope.getRecords();
			}
			else{
				alert(data.message);
			}
		});
	};
	
	$scope.products = [];
	$scope.productsSelected = [];
	$scope.loadProductList = function(){
		$http
		.get('/admin/products/json/all')
		.then(function(resp){$scope.products = resp.data;});
	};
	$scope.loadProductList();
	
	$scope.selectProduct = function(){
		var present = false;
		for(var i=0;i<$scope.productsSelected.length;i++){
			if($scope.productsSelected[i]._id == $scope.productSelect._id){
				present = true;
				break;
			}
		}
		
		if(!present){
			$scope.productsSelected.push($scope.productSelect);
		}
		
		$scope.productSelect = "";
	};
	
	$scope.removeProduct = function(){
		for(var i=0;i<$scope.productsSelected.length;i++){
			if($scope.productsSelected[i]._id == this.ps._id){
				$scope.productsSelected.splice(i,1);
				break;
			}
		}
	};
	
	$scope.updateDiscount = function(){
		
		$scope.pageLevelError = '';
		if($scope.discountPercentage < 0 || $scope.productsSelected.length == 0){
			if($scope.discountPercentage < 0){
				$scope.pageLevelError = 'Invalid Discount';
			}
			else {
				$scope.pageLevelError = 'Please select products';
			}
			return false;
		}
		
		var selectedProducts = $scope.productsSelected.map(function(product){
			return product._id;
		});
		

		$http.post('/admin/ctrls/discountcontrol', 
				{'attr':$scope.discountPercentage, 'value' : selectedProducts.join()})
		.then(function(resp){
			var data = resp.data;
			if(data.success){
				_lbFns.pSuccess('Discounts Applied\n' + data.message);
				
				$scope.discountPercentage = 0;
				$scope.productsSelected = [];
				
			}
			else{
				alert(data.message);
			}
		});
	};
    
};

crApp
//below fns are defined in angular-general-functions.js
.config(appConfigs)
.run(appRunFns) 
//end fns defined in angular-general-functions.js


.controller('defaultRouteCtrl', defaultCtrlr);
