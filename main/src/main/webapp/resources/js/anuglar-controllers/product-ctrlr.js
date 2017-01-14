var productCtrlr = function($scope, $http, $rootScope){
	
	$scope.prices = [];
	$scope.productId = $('#productId').val();
	$scope.productSelected = {};
	$scope.outofstock = false;
	$scope.productStockStat = $('#productStockStat').val();
	$scope.featuredImg = $('#featuredImg').val();
	$scope.variableProduct = document.getElementById('variableProduct')?true:false;
	$scope.addingToCart = false;
	$scope.quantity = 1;
	$scope.successMsg = '';
	$scope.productInCart = 0;
	
	$scope.lineItem = {};
	
	$scope.getProductPrice = function(){
		
		$http.get(_lbUrls.getprd + $scope.productId + '/price', {
			params : { 
				'hidepop' : true  //tells the config not to show the loading popup
			}
		})
		.then(function(resp){
			$scope.prices = resp.data;		
			
			
			if($scope.prices){

				//We don't want the page to load with outofstock, 
				//hence we first set it to true and then check individual items 
				$scope.outofstock = true;
				
				//Sort the data
				$scope.prices.sort(function(a,b){return a.regPrice - b.regPrice;});
				
				//Set the first instock item as selected
				for(var i=0; i<$scope.prices.length; i++){
					if($scope.prices[i].stockStat == 'instock'){
						$scope.prices[i].selected = true;
						$scope.productSelected = $scope.prices[i];
						$scope.outofstock = false;
						break;
					}

				}
				
				//If its outofStock at product level, then update every option to be outofstock
				if($scope.productStockStat=='outofstock'){
					$scope.prices.forEach(function(e){
						e.selected = false;
						e.stockStat = 'outofstock';
					});
					
					$scope.outofstock = true;
				}
			}

		});	
		
	};
	
	$scope.selectPrdOption = function(){
		
		if(this.price.stockStat=='outofstock')
			return;
		
		$scope.prices.forEach(function(e){
			e.selected = false;
		});
		
		this.price.selected = true;
		$scope.productSelected = this.price;
		
		if($scope.productSelected.img && $scope.productSelected.img != 'null'){ 
			$scope.zoomFn.destroy();
			
			$img.find('img').attr('src','/files/view/'+ $scope.productSelected.img);
			$img.data('variationImg', true);
			
			$scope.zoomFn.activate();
		}
		else{
			if(!$img.data('variationImg')) return;

			$scope.zoomFn.destroy();
			
			$img.find('img').attr('src','/files/view/'+ $scope.featuredImg);
			$img.data('variationImg', false);
			
			$scope.zoomFn.activate();
			
		}

	};
	
	$scope.addToCart = function(){
		$scope.errorMsg = '';
		$scope.successMsg = '';
		
		if($scope.variableProduct && !$scope.productSelected){
			$scope.errorMsg  = 'Please select a product.';
			return false;
		}
		
		if(!$scope.quantity || $scope.quantity<=0){
			$scope.errorMsg  = 'Minimum quantity should be 1.';
			return false;
		}
		
		$scope.addingToCart = true;		
			
		$scope.lineItem = {
				name : $('#productName').val(),
				qty : $scope.quantity,
				productId : $scope.productId,
				img : $scope.productSelected.img ? $scope.productSelected.img : $scope.featuredImg,
				variationId : $scope.productSelected._id ? $scope.productSelected._id : 0
		};
		
		$http.post(_lbUrls.addtocart+'?hidepop', $scope.lineItem)
		.then(function(resp){
			
			if(resp.data && resp.data.success){
				
				$scope.productInCart = resp.data.productCount;
				$rootScope.rootCartCount = resp.data.cartCount;
				$.cookie('lbbagnumber', resp.data.orderId, {expires:30, path:'/'});
				$scope.successMsg = 'Item added to cart.';
			}
			else {
				$scope.errorMsg  = resp.data.message;
			}

			$scope.addingToCart = false;
		},
		function(resp){
			if(resp.status == 403){
				$scope.errorMsg  = "Your browser was idle for long. " + 
					"Please refresh the page and add the item to cart again.";
			}
			else {
				$scope.errorMsg  = "There was some error creating the order. " + 
					"Please try later. If problem persists, please call the customer care.";
			}

			$scope.addingToCart = false;
		});
	};
	
	
	$scope.getProductsInCart = function(){
		$http.get(_lbUrls.cart + 'productInCart', {
			params : { 
				'pid': $scope.productId,
				'hidepop' : true  //tells the config not to show the loading popup
			}
		})
		.then(function(resp){
			if(resp.data.success)
				$scope.productInCart = resp.data.message;
		});
	};
	
	
	if($scope.productId){
		if($scope.variableProduct){
			$scope.getProductPrice();
		}
		
		else{
			if($scope.productStockStat != 'instock'){
				$scope.outofstock = true;
			}
		}
		
		if($scope.productStockStat == 'instock'){
			$scope.getProductsInCart();
		}
	}
	
	
	/* Multiple Product Img hovering*/
	$scope.changeMainImg = function($event){
		var $currLi = $($event.target).closest('li');
		
		if($currLi.hasClass('.selected')) return;
		
		//destroy existing zoom
		$scope.zoomFn.destroy();
		
		//setup new image
		
		var newImg = $currLi.find('img'),
		newImgSrc = newImg.attr('data-zoom') ? newImg.attr('data-zoom').replace('.jpg','-600x600.jpg') : '';
		if(newImgSrc === '') return;
		
		$img.find('img').attr('src', newImgSrc);
		$img.find('img').attr('data-zoom', newImg.attr('data-zoom'));
		$currLi.addClass('selected').siblings().removeClass('selected');
		
		//activate zoom
		$scope.zoomFn.activate();
	};
	
	
	/* Review Section */
	$scope.showReviews = false;
	$scope.reviews = [];
	$scope.reviewSortIsOpen = false;
	$scope.reviewSortOptions = [
		{text : 'Sort by - Most recent reviews', value : 'latest'},
		{text : 'Sort by - Oldest reviews', value : 'old'},
		{text : 'Sort by - Most rated reviews', value : 'toprated'},
		{text : 'Sort by - Lowest rated reviews', value : 'lowrated'}
	];
	$scope.reviewSortSelected = $scope.reviewSortOptions[0];
	
	$scope.getProductReviews = function(){
		
		$http.get(_lbUrls.getprd + $scope.productId + '/topreviews', {
			params : { 
				's' : $scope.reviewSortSelected.value,
				'hidepop' : true  //tells the config not to show the loading popup
			}
		})
		.then(function(resp){
			$scope.reviews = resp.data;
		});	
		
	};	
	
	$scope.changeReviewSort = function(){
		$scope.reviewSortSelected = this.rso;
		$scope.getProductReviews();
	};

	$scope.toggleDropdown = function($event) {
		$event.preventDefault();
	    $event.stopPropagation();
	    $scope.reviewSortIsOpen = !$scope.reviewSortIsOpen;
	};

	(function reviewInit(){
		if($('#productReviewsIndicator').length > 0){
			$scope.showReviews = true;
			$scope.getProductReviews();
		}
	})();
	
	
	
	/*Activate Image Zoom*/
	var $img = $('.prdpage-img');
	$scope.zoomFn = {
			activate : function(){
				
				var url = $img.find('img').attr('data-zoom');
				if(url && url !==''){
					
					$img.zoom({
						url : url,
						callback: function(){
							$(this).parent().addClass('loaded');
						}
					});					
				}
			},
			
			destroy : function(){
				$img.parent().removeClass('loaded');
				$img.trigger('zoom.destroy');
			}
	};
	$scope.zoomFn.activate();
};