//Create a random number for version control and avoid caching
var versionCtrl = '?v=' + Math.random();

var cmApp = angular.module(
	'contentManagementApp', [
         'ngSanitize',
         'ui.bootstrap',
         'uploadModule',
         'ngFileUpload'
     ]
);


/**
 * All the controller functions are implemented here. 
 * Scroll to the bottom to see it being attached to the App.
 */ 
var 
defaultCtrlr = function($scope, $http, $filter, uploadService, $rootScope){
	
	$scope.pageLevelError = '';
	$scope.slides = [];
	$scope.newSlider;
	$scope.saveSliderText = "Save Slide";
	
	$scope.resetSliderVar = function(){
		$scope.newSlider = { 
				active: true,
				sliderInfo: {
					modal: false 
				}
			};
	};
	$scope.resetSliderVar();
	
	
	$scope.getSlides = function(){
		
		$http.get('/admin/cms/slides/json/all')
		.then(function(resp){			
			$scope.slides = resp.data;
		});	
		
	};	
	$scope.getSlides();
	
	
	$scope.saveSlideRecord = function(){
		
		$scope.saveSliderText = "Saving...";
		var formScope = this;
		
		$http.post('/admin/cms/slides/saverecord', $scope.newSlider)
		.then(function(resp){
			var data = resp.data;
			if(data.success){
				
				formScope.newSlideForm.$setPristine();
				formScope.newSlideForm.$setUntouched();
				
				$scope.resetSliderVar();
				$scope.saveSliderText = "Save Slide";
				
				_lbFns.pSuccess('Slide added');
				
				$scope.getSlides();
			}
			else{
				alert(data.message);
			}
		});
	};
	
	
	$scope.editSlide = function(){
		$scope.newSlider = this.slide;
		$scope.showAddSlide = true;
	};
	
	
	
	
	/**UPLOAD SPECIFIC FUNCTIONS*/
	$scope.cdnPath = uploadService.cdnPath;
	uploadService.config.productUploader = true;
	
	$scope.afterSelect = function(){
		if(uploadService.selectedFiles){
			$scope.newSlider.sliderInfo.sliderImg = uploadService.selectedFiles[0].location;
			$scope.newSlideForm.$setDirty();
		}
	};
	
	$scope.selectImage = function(){
		uploadService.config.cb = $scope.afterSelect;
		uploadService.config.fields = {path : '/sliders/'};
		uploadService.config.productUploader = false;
		uploadService.config.filter = "slider";
		uploadService.showGallery($rootScope);
	};
	
	$scope.afterSelectSM = function(){
		if(uploadService.selectedFiles){
			$scope.newSlider.sliderInfo.sliderImgSM = uploadService.selectedFiles[0].location;
			$scope.newSlideForm.$setDirty();
		}
	};
	
	$scope.selectImageSM = function(){
		uploadService.config.cb = $scope.afterSelectSM;
		uploadService.config.fields = {path : '/sliders/'};
		uploadService.config.productUploader = false;
		uploadService.config.filter = "slider";
		uploadService.showGallery($rootScope);
	};
	
	
	
	
	
	
	
	
	
	$scope.products = [];
	$scope.productsSelected = [];
	$scope.loadProductList = function(){
		$http
		.get('/admin/products/json/all')
		.then(function(resp){$scope.products = resp.data;});
	};
	//$scope.loadProductList();
	
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

cmApp
//below fns are defined in angular-general-functions.js
.config(appConfigs)
.run(appRunFns) 
//end fns defined in angular-general-functions.js


.controller('defaultRouteCtrl', defaultCtrlr)

//This is defined in angular-upload-service.js. Common fn for uploads
.controller('uploadCtrlr', uploadCtrlr);
