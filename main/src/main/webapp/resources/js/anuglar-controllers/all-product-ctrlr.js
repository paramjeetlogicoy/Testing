var allProductCtrlr = function($scope, $http, $rootScope, $templateRequest, $compile){

	$scope.prices = [];
	$scope.productPrices = {};
	$scope.currentPid = 0;
	$scope.flipBack = function(event){
		angular.element(event.target).closest('li').find('.product-item-card').removeClass('showInfo showOptions');
	};
	
	$scope.showInfo = function(event){		
		//Unflip all flipped elements
		angular.element('.product-item-card').removeClass('showInfo showOptions');
		angular.element(event.target).closest('li').find('.product-item-card').addClass('showInfo');
	};
	
	$scope.showOptions = function(event){
		
		//Unflip all flipped elements
		angular.element('.product-item-card').removeClass('showInfo showOptions');
		
		var $angularElement = angular.element(event.target).closest('li');
		$angularElement.find('.product-item-card').addClass('showOptions');
		
		var variation = $angularElement.data('var'),
			price = parseFloat($angularElement.data('p')),
			salePrice = parseFloat($angularElement.data('sp'));
		
		$scope.currentPid = $angularElement.data('pid');
		
		if($scope.currentPid !== 0){	
			
			$scope.prices = [];
			
			if(!$scope.productPrices[$scope.currentPid]){				

				
				if(variation){
					
					//Variable product, get prices					
					$http.get(_lbUrls.getprdprice + $scope.currentPid + '/price', {
						params : { 
							'hidepop' : true  //tells the config not to show the loading popup
						}
					})
					.then(function(resp){
						$scope.prices = resp.data;	
						
						if($scope.prices){							
							$scope.loadTemplate($angularElement);
						}
					});	
				}
				
				else{
					
					//Simple product, build prices from the available info					
					$scope.prices = [{
						"_id":0,
						"variationId":0,
						"productId":$scope.currentPid,
						"variation":[],
						"stockCount":0,
						"regPrice":price,
						"salePrice":salePrice,
						"stockStat":"instock",
						"img":null,
						qty:0
					}];

					$scope.loadTemplate($angularElement);
					
				}
				
				
			} else {
				//The template was previously loaded. So noop()
			}
		}
	};
	
	
	$scope.loadTemplate = function($angularElement){

		//load productOptions		
		$templateRequest("productOptionsTemp")
		.then(function(html){
		      var template = angular.element(html);
		      $angularElement.find('.product-options')
		      	.removeClass('empty')
		      	.empty()
		      	.append(template);
		      
		      $compile(template)($scope);
		 });
	};
	
	$scope.showBiggerImg = function(event){
		event.preventDefault();
		
		var $angularElement = angular.element(event.currentTarget),
		imgSrc = $angularElement.attr('href');
		
		$('#prodImgModal .modal-content').html($('<img />').attr({'src':imgSrc, 'class':'fit'}));				
		$('#prodImgModal').modal('show');		
	};
	
	$scope.closeImgModal = function(){
		$('#prodImgModal').modal('hide');	
	};
	
	$scope.setPageAlert = function(alert){
		$scope.pageLevelAlert = alert;
	};
};