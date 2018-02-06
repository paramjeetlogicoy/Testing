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
	$scope.sliders = [];
	$scope.newSlider;
	$scope.saveSliderText = "Save Slide";
	$scope.modalHtmlErr = false;
	
	$scope.resetSliderVar = function(){
		$scope.newSlider = { 
				active: true
			};
	};
	$scope.resetSliderVar();
	
	
	$scope.getSlides = function(){
		
		$scope.sliders = [];
		
		$http.get('/admin/cms/slides/json/all')
		.then(function(resp){			
			var sliders = resp.data;
			
			var homeSliders = [];
			if(sliders != null && sliders.length > 0){
				sliders.forEach(function(slider){
					if(slider.sliderName == 'homepage')
						homeSliders.push(slider)
				});
				
				$scope.sliders.push({
					name: 'homepage',
					slides: homeSliders
				});
			}
		});	
		
	};	
	$scope.getSlides();
	
	
	$scope.saveSlideRecord = function(){

		var formScope = this;
		$scope.modalHtmlErr = false;
		if($scope.newSlider.modal){
			$scope.newSlider.modalHtml = modalHtmlEditor.getData();
			
			if($scope.newSlider.modalHtml == '') {
				$scope.modalHtmlErr = true;
				return;
			}
		}
		
		$scope.newSlider.title = $scope.newSlider.title.replace(/[^a-z0-9 _-]/gi,''); //remove invalid characters
		
		
		$scope.saveSliderText = "Saving...";
		
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
				modalHtmlEditor.setData("");
			}
			else{
				alert(data.message);
			}
		});
	};
	
	
	$scope.editSlide = function(){
		$scope.newSlider = this.slide;
		$scope.showAddSlide = true;
		
		if(modalHtmlEditor) 
			modalHtmlEditor.setData($scope.newSlider.modalHtml);
		
		/* Scroll up to the form*/
	     $('html,body').animate({scrollTop:250},500);
	};
	
	
	$scope.activateSlide = function(){
		var pg = this.slide;
		pg.active = true;
		
		changeActiveStatus(pg);
	};
	
	
	$scope.deactivateSlide = function(){
		var pg = this.slide;
		pg.active = false;
		
		changeActiveStatus(pg);
	};
	
	var changeActiveStatus = function(pg){
		
		$http.post('/admin/cms/slides/changestatus', pg)
		.then(function(resp){
			var data = resp.data;
			if(data.success){
				_lbFns.pSuccess('Slide status changed');
				
				$scope.getSlides();
			}
			else{
				alert(data.message);
			}
		});
	};
	
	
	$scope.deleteSlide = function(){

		if(this.slide){
			$http.post('/admin/cms/slides/removeslide', this.slide)
			.then(function(resp){
				var data = resp.data;
				if(data.success){
					_lbFns.pSuccess('Slide removed');
					
					$scope.getSlides();
				}
				else if(data.message){
					alert(data.message);
				}
				else{
					alert('There was some error removing the slide.');
				}
			});
		}
	};
	
	$scope.publishSlide = function(){

		var sliderName = this.slider.name,
		pageSlider = { 'sliderName': sliderName };
		
		$http.post('/admin/cms/slides/publish', JSON.stringify(pageSlider))
		.then(function(resp){
			var data = resp.data;
			if(data.success){
				_lbFns.pSuccess('Publish successful');
			}
			else{
				alert(data.message);
			}
		});
		
	};
	
	
	/**UPLOAD SPECIFIC FUNCTIONS*/
	$scope.cdnPath = uploadService.cdnPath;
	uploadService.config.productUploader = true;
	
	$scope.afterSelect = function(){
		if(uploadService.selectedFiles){
			$scope.newSlider.sliderImg = uploadService.selectedFiles[0].location;
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
			$scope.newSlider.sliderImgSM = uploadService.selectedFiles[0].location;
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
