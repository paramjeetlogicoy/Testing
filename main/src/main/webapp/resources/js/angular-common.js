var lbApp = angular.module(
	'lbApp', [
	     'ngRoute',
	     'ngAnimate',
         'ngSanitize',
         'ui.bootstrap'
     ]
);


/**
 * All the controller functions are implemented here. 
 * Scroll to the bottom to see it being attached to the App.
 */ 
var 
allProductCtrlr = function($scope, $http){
	
	$scope.products = [];
	$scope.categories = [];
	$scope.categoryFilter = '';
	
	$scope.getProducts = function(){
		
		$http.get(_lbUrls.allprds, {
			params : { 
				'hidepop' : true  //tells the config not to show the loading popup
			}
		})
		.success(function(data){
			if(data.success){
				$scope.products = data.products;
				$scope.categories = data.categories;

			}
			else{	
				$scope.pageLevelError = "There was some error getting the products."
					+" Please contact the support";
			}

		}).error(function(){

			$scope.pageLevelError = "There was some error getting the products."
				+" Please contact the support";
		});	
		
	};
	
	$scope.filterProducts = function(){
		$scope.categoryFilter = this.cat.name;
		$scope.productFilter = '';
	};
	
	$scope.getProducts();
},

loginCtrlr = function($scope, $http){
	
	$scope.user = {};
	
	$scope.login = function(){
		var req = {
				 method: 'POST',
				 url: _lbUrls.login,
				 headers: {
					 'Content-Type': 'application/x-www-form-urlencoded'
				 },
				 params: {
					'username':$scope.user.username, 
					'password':$scope.user.password,
					'rememberme':true
				}
		};

		$http(req)
		.then(function(resp){
			if(resp.data){
				if(resp.data.success){
					if($('#redirectURL').val()!=''){
						location.href = $('#redirectURL').val();
					}
					else 
						location.href = resp.data.success;
				}
				else if(resp.data.authfailure){
					$scope.pageLevelAlert = "Incorrect username password.";
				}
				else{
					$scope.pageLevelAlert = "There was some error. Please try again later.";
				}

			}
			else{	
				$scope.pageLevelAlert = "There was some error. Please try again later.";
			}

		},
		function(){
			$scope.pageLevelAlert = "There was some error. Please try again later.";
		});	
		
		return false;
	};
},

productCtrlr = function($scope, $rootScope, $http){
	
	$scope.prices = [];
	$scope.productId = $('#productId').val()
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
		
		$http.get(_lbUrls.getprdprice + $scope.productId + '/price', {
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
				$scope.errorMsg  = "Your browser was idle for long. "
					+"Please refresh the page and add the item to cart again.";
			}
			else {
				$scope.errorMsg  = "There was some error creating the order. "
					+"Please try later. If problem persists, please call the customer care.";
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
	
	/*Activate Image Zoom*/
	var $img = $('.prdpage-img-container');
	$scope.zoomFn = {
			activate : function(){
				$img.zoom({
					callback: function(){
						$(this).parent().addClass('loaded');
					}
				});
			},
			
			destroy : function(){
				$img.trigger('zoom.destroy');
			}
	};
	$scope.zoomFn.activate();
},

cartCtrlr = function($scope, $rootScope, $http){
	
	$scope.order = {};	
	$scope.pageLevelAlert='';	
	$scope.user = {};
	$scope.addressValidated = false;
	$scope.cartLoading = true;
	$scope.sales = [];
	
	if(_globalUserId)
		$scope.user._id = _globalUserId;
	
	$scope.getCart = function(){
		$http.get(_lbUrls.getcart, {
			params : { 
				'hidepop' : true  //tells the config not to show the loading popup
			}
		})
		.then(function(resp){
			
			$scope.cartLoading = false;
			
			$scope.order = resp.data.order;
			$scope.updateSales();
			
			$scope.user = resp.data.user?resp.data.user:{};
			
			if($scope.order && $scope.order.billing){				
				$scope.user.billing = $scope.order.billing;
				$scope.addressLogic();
			}
			
			else if($scope.user && $scope.user.billing){
				$scope.cartAddress.$setDirty();
			}
			
			else {				
				
				if(_globalUserId && $scope.order){
					$http.get('/customer/json/' + _globalUserId)
					.then(
							function(resp){
								if(resp.data){
									$scope.user = resp.data;
									if($scope.user.billing){
										$scope.cartAddress.$setDirty();
									}
								}
							},
							function(){});
				}
			}
		},
		function(){
			$scope.cartLoading = false;
		});
	};
	
	$scope.getCart();
	
	$scope.addressLogic = function(){
		
		if($scope.user.billing && ($scope.user.billing.address1 != '' 
			|| $scope.user.billing.city != ''
			|| $scope.user.billing.state != ''
			|| $scope.user.billing.zip != '')){
			
			$scope.addressValidated = true; 
			
			$('.delivery-address').empty()
			.append($('<img />')
			.attr({'src' : $scope.getAddressMapLink(), 'class':'fit'}));
		}
	};
	
	$scope.itemRemove = function(){
		
		$scope.pageLevelAlert='';
		
		if($scope.order && this.item){
			var req = {
					method: 'DELETE',
					url: _lbUrls.cart + 'removeitem',
					params: {'oid' : $scope.order._id, 'vid' : this.item.variationId}
			};
			
			$http(req)
			.then(
					function(resp){
						if(resp.data && resp.data.success){
					
							$rootScope.rootCartCount = resp.data.cartCount;
							
							$scope.order = resp.data.order;
							$scope.updateSales();
							
							_lbFns.pSuccess('Item removed.');
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
		else {
			$scope.pageLevelAlert = $rootScope.errMsgPageRefresh;
		}
	};
	
	$scope.updateCart = function(){
		
		$scope.pageLevelAlert = '';
		
		if(this.item && this.item.qty && this.item.qty !=0){
			$http.get(_lbUrls.cart + 'updatecart', {
				params : { 
					'oid' : $scope.order._id,
					'vid' : this.item.variationId,
					'pid' : this.item.productId,
					'qty' : this.item.qty
				}
			})
			.success(function(resp){
				if(resp.success){
					
					$rootScope.rootCartCount = resp.cartCount;
					$scope.order = resp.order;
					$scope.updateSales();
					
					_lbFns.pSuccess('Order Updated.');		
				}
				else{
					$scope.pageLevelAlert = resp.message;
				}
			});
		}
		else if(!this.item.qty){
			$scope.pageLevelAlert = "Please provide a valid quantity";
			return false;
		}
		else {
			$scope.pageLevelAlert = $rootScope.errMsgPageRefresh;
		}
	};
	
	$scope.saveDeliveryAddress = function(){
		
		if($scope.order._id && $scope.cartAddress.$valid && $scope.cartAddress.$dirty){
			
			$http.post(_lbUrls.cart + $scope.order._id + '/savedeliveryaddr',$scope.user)
			.then(
					function(resp){
						if(resp.data && resp.data.success){
							_lbFns.pSuccess('Address Saved.');
							
							$scope.addressValidated = true; 
							
							$('.delivery-address').empty()
							.append($('<img />').attr({'src' : $scope.getAddressMapLink(), 'class':'fit'}));
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
	
	$scope.saveDeliveryNotes = function(){
		
		if($scope.order._id && $scope.order.notes.deliveryNotes){			
			$http.post(_lbUrls.cart + $scope.order._id + '/savedeliverynote?hidepop',
				{'deliveryNotes':$scope.order.notes.deliveryNotes});			
		}
		
	};	
	
	
	$scope.removeCoupon = function(){
		
		$scope.pageLevelAlert = "";
		var couponCode = this.item.name;
		
		if(couponCode){
			$http.get(_lbUrls.cart + 'removecoupon/' + couponCode,{
				params : {
					'hidepop' : true,
					'oid' : $scope.order._id
				}
			})
			.then(
					function(resp){
						if(resp.data && resp.data.success){
							_lbFns.pSuccess('Promocode removed');
							
							//Update cart.
							$scope.getCart();
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
		else{
			$scope.pageLevelAlert = "Invalid promo code";
		}
			
	}
	
	
	$scope.applyCoupon = function(){
		
		$scope.pageLevelAlert = "";
		
		if($scope.couponCode){
			$http.get(_lbUrls.cart + 'applycoupon/' + $scope.couponCode,{
				params : {
					'hidepop' : true,
					'oid' : $scope.order._id
				}
			})
			.then(
					function(resp){
						if(resp.data && resp.data.success){
							_lbFns.pSuccess('Promocode applied');
							$scope.couponCode = '';
							
							//Update cart.
							$scope.getCart();
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
		else{
			$scope.pageLevelAlert = "Invalid promo code";
		}
			
	}
	
	
	$scope.placeOrder = function(){
		
		if($scope.order._id && $scope.user._id){			
			$http
			.post(_lbUrls.cart + $scope.order._id + '/placeorder',$scope.order)
			.then(
					function(resp){
						if(resp && resp.data){
							if(resp.data.success){
								location.href = "/confirmation/" + resp.data.message;
							}
							else{
								$scope.pageLevelAlert = resp.data.message;
							}
						}
						else{
							$scope.pageLevelAlert = 'There was error placing the order. '
								+'Please refresh the page and try again later. '
								+'If problem persists, please contact customer care.';
						}
					},
					function(){
						$scope.pageLevelAlert = $rootScope.errMsgPageRefresh;
					}
			);			
		}
		else{
			$scope.pageLevelAlert = 'Unable to find an order. '
				+'Are you sure you have items in your cart and '
				+'you are logged in to your account?';
		}
		
	};
	
	/*When ever there is a change in $scope.order, this function needs to be called*/
	$scope.updateSales = function(){
		console.log('order changed');
		$scope.sales = [];
		if($scope.order.lineItems && $scope.order.lineItems.length>0){
			$scope.order.lineItems.forEach(function(item){
				if(item.type=='item' 
					&& item.promo == 's'){
					
					var productName = item.name.length>15?item.name.substr(0,15)+'...':item.name,
					discount = (item.cost - item.price)*item.qty;
					
					if(!isNaN(discount) && discount > 0){						
						$scope.sales.push({
							'name' : productName,
							'price' : discount
						});
					}
					
				}
			});
		}
	};
	
	$scope.editAddress = function(){
		$scope.addressValidated = false;
		$scope.cartAddress.$setDirty();
	};
	
	$scope.getAddressMapLink = function(){
		if($scope.user && $scope.user.billing) {
			var mapLink = {
					'markers':'color:blue|label:S|' + $scope.getAddress(),
					'zoom':17,
					'scale':2,
					'size':'280x280',
					'center':$scope.getAddress(),
					'key':'AIzaSyDOOuxNzzE247y4HbG9B5J2yM8vzzhegCU'};
			
			
			return "https://maps.googleapis.com/maps/api/staticmap?" + $.param(mapLink);
		}
		else
			return null;
	};
	
	$scope.getAddress = function(){
			return $scope.user.billing.address1 + ' ' + $scope.user.billing.city + ' ' + 
				$scope.user.billing.state + ' ' + $scope.user.billing.zip;
	};
	
	$scope.goodToProceed = function(){
		
		return $scope.order 
			&& !$scope.emptyCart() 
			&& $scope.addressValidated;
	};
	
	$scope.emptyCart = function(){		
		var empty = true;
		if($scope.order.lineItems && $scope.order.lineItems.length>0){
			for(var i=0; i<$scope.order.lineItems.length;i++){
				if($scope.order.lineItems[i].type=='item' 
					&& $scope.order.lineItems[i].instock){
					
					empty = false;
					break;
				}
			}
		}
		
		return empty;
	};
	
	$scope.promoApplied = function(){		
		var applied = false;
		if($scope.order.lineItems && $scope.order.lineItems.length>0){
			for(var i=0; i<$scope.order.lineItems.length;i++){
				if($scope.order.lineItems[i].type=='coupon' 
					&& $scope.order.lineItems[i].instock){
					
					applied = true;
					break;
				}
			}
		}
		
		return applied;
	}
};

lbApp
//below fns are defined in angular-general-functions.js
.config(appConfigs)
.run(appRunFns) 
//end fns defined in angular-general-functions.js


.controller('allProductCtrlr', allProductCtrlr)
.controller('productCtrlr', productCtrlr)
.controller('loginCtrlr', loginCtrlr)
.controller('cartCtrlr', cartCtrlr);