var lbApp = angular.module(
	'lbApp', [
	     'ngRoute',
	     'ngAnimate',
         'ngSanitize',
         'ui.bootstrap',
         'ngFileUpload'
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
		
		$http.get(_lbUrls.allprds + '/published', {
			params : { 
				'hidepop' : true  //tells the config not to show the loading popup
			}
		})
		.success(function(data){
			if(data.success){
				
				//Remove the products loaded by thymeleaf
				angular.element('#thymelead-loader').remove();
				
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

categoryCtrlr = function($scope, $http){
	
	$scope.products = [];
	$scope.categoryName = _pageCatName; /*Defined in the HTML page*/
	
	$scope.getProducts = function(){
		
		$http.get(_lbUrls.catprds + $scope.categoryName, {
			params : { 
				'hidepop' : true  //tells the config not to show the loading popup
			}
		})
		.then(function(response){
			var data = response ? response.data : '';
			if(data){
				$scope.products = data;
			}
			else{	
				$scope.pageLevelError = "There was some error getting the products."
					+" Please contact the support";
			}

		},
		function(){
			$scope.pageLevelError = "There was some error getting the products."
				+" Please contact the support";
		});	
		
	};
	
	if($scope.categoryName != ''){ 
		$scope.getProducts();
	}
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
				if(resp.data.pending){
					location.href = resp.data.pending;
				}
				else if(resp.data.success){
					if($('#redirectURL').val()!=''){
						location.href = $('#redirectURL').val();
					}
					else 
						location.href = resp.data.success;
				}
				else if(resp.data.authfailure){
					$scope.pageLevelAlert = resp.data.authfailure;
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

registerCtrlr = function($scope, $http, Upload){
	
	var today = new Date();
	
	$scope.user = {'identifications':{}, 'marketing':{}};
	$scope.today = today.getFullYear() + "-" + (today.getMonth() + 1) + "-" + today.getDate();
	
	$scope.hearAboutOptions = ['WeedMaps', 'Yelp', 'Facebook', 'Leafly', 'Friends & Family', 'La Weekly', 'Other'];
	
	$scope.register = function(){
		
		$scope.pageLevelAlert = "";
		$('.recofile-group, .idfile-group').removeClass('has-error');
		
		if($scope.registerForm.$valid 
				&& $scope.recoFile 
				&& $scope.idFile){
			
			$http.post(_lbUrls.register, $scope.user)
			.then(function(response){
				var resp = response.data;	
				if(resp.success){
					
					//Remove files from localStorage
					localStorage.removeItem('recoFile');
					localStorage.removeItem('idFile');
					
					alert('Account created');
				}
				else{
					$scope.pageLevelAlert = resp.message; 
				}
			},
			
			function(){
				$scope.pageLevelAlert = "There was some error creating your account. "
					+"Please try later. If problem persists, please call the customer care.";
			});	
		}
		
		else{
			
			if(!$scope.recoFile){
				$('.recofile-group').addClass('has-error');
			}
			
			if(!$scope.idFile){
				$('.idfile-group').addClass('has-error');
			}
			
			if(!$scope.registerForm.$valid){
				$scope.pageLevelError = 'Please correct the highlighted sections'; 
			}
			
			$('html,body').scrollTop($('.has-error').offset().top-120);
		}
	};
	
	
	$scope.removeFile = function(type){
		
		if(type == 'id' && $scope.idFile){			
			$http.post('/files/delete/id/' + $scope.idFile._id);
			$scope.idFile = null;
			localStorage.removeItem('idFile');
		}
		
		else if(type == 'reco' && $scope.recoFile){
			$http.post('/files/delete/id/' + $scope.recoFile._id);
			$scope.recoFile = null;
			localStorage.removeItem('recoFile');
		}
	};
	
	$scope.invalidFname = function(){		
		return $scope.registerForm.firstname.$invalid 
			&& !$scope.registerForm.firstname.$pristine;
	};
	
	$scope.invalidLname = function(){		
		return $scope.registerForm.lastname.$invalid 
			&& !$scope.registerForm.lastname.$pristine;
	};
	
	$scope.invalidEmail = function(){		
		return $scope.registerForm.email.$invalid 
			&& !$scope.registerForm.email.$pristine;
	};
	
	$scope.invalidUname = function(){		
		return $scope.registerForm.username.$invalid 
			&& !$scope.registerForm.username.$pristine;
	};
	
	$scope.invalidPassword = function(){		
		return $scope.registerForm.password.$invalid 
			&& !$scope.registerForm.password.$pristine;
	};
	
	$scope.invalidCPassword = function(){		
		return !$scope.registerForm.confirmpassword.$pristine
			&& ($scope.user.password != $scope.confirmPassword);
	};
	
	$scope.invalidPhone = function(){		
		return $scope.registerForm.phone.$invalid 
			&& !$scope.registerForm.phone.$pristine;
	};

	
    $scope.$watch('idfileobj', function () {
        new $scope.upload($scope.idfileobj, 'id');
    });

	
    $scope.$watch('recofileobj', function () {
        new $scope.upload($scope.recofileobj,'reco');
    });
    
    
    $scope.upload = function (file, type) {
      if (file) {
	        var fileInfo = {progress : false, element : null};
	        
	        Upload.upload({
	            url: '/files/upload',
	            fields : {path : '/user/'+$scope.today + "/", ctrl : 'controlled'},
	        	file: file,
	        	fileInfo : fileInfo
	            
	        }).then(
	
	    	        function (resp) { //Success
		    	        
			            if(resp.config.fileInfo)
			            	resp.config.fileInfo.element.closest('.row').remove();
			
			           if(resp.data.results){
			        	   var img = resp.data.results[0];
			        	   
			        	   if(img){
				        	   if(type && type=='id'){
				        		   $scope.user.identifications.idCard = img.location;
				        		   $scope.idFile = img;
				        		   localStorage.setItem('idFile', JSON.stringify(img));
				        	   }
				        	   else if(type && type=='reco'){
				        		   $scope.user.identifications.recomendation = img.location;
				        		   $scope.recoFile = img;
				        		   localStorage.setItem('recoFile', JSON.stringify(img));
				        	   }
			        	   }
			           }
			            
			        },
	
	
	
	    	        function (resp) {//Error
			            if(resp.config.fileInfo)
			            	resp.config.fileInfo.element.parent().html('<div class="progress-bar progress-bar-danger"style="width: 100%">Error Uploading File</div>');
			            
			            console.log('Error uploading the file');
			        },
	
	
	    	        function (evt) { //Progress
			        	if(!evt.config.fileInfo.progress){
				        	var f = evt.config.fileInfo;
				        	
				        	var container = '.progress-wrapper ';
				        	if(type) container = '.' + type + '-progress-wrapper';
				        	
			        		f.progress = true;
			        		f.element = $('<div />').attr({'class': 'progress-bar progress-bar-success', 'role':'progressbar','style':'width:0%'});
			        		
			        		$('<div class="row"><div class="col-xs-4 file-name-holder">' 
			                		+ evt.config.file.name 
			                		+ '</div><div class="col-xs-8"><div class="progress"></div></div>')
			                		.appendTo(container)
			                		.find('.progress')
			                		.append(f.element);
			        	}
	
			            if(evt.config.fileInfo){
			            	var progressPercentage = parseInt(100.0 * evt.loaded / evt.total);
			            	evt.config.fileInfo.element.css('width', progressPercentage+'%').html(progressPercentage+'%');
			            }
			        }
			 );
      }
    };
    
    $scope.init = function(){
    	
    	try {
    		
        	if(localStorage.getItem('recoFile')){
        		$scope.recoFile = JSON.parse(localStorage.getItem('recoFile')); 
      		   	$scope.user.identifications.recomendation = $scope.recoFile.location;
     	   	}
        	
        	if(localStorage.getItem('idFile')){
        		$scope.idFile = JSON.parse(localStorage.getItem('idFile'));
        		$scope.user.identifications.idCard = $scope.idFile.location;
        	}
    	}catch(e){}
    };
    
    $scope.init();
}, 

resetCtlr = function($scope, $http, $rootScope){
	
	$scope.successNotification = "";
	
	$scope.invalidPassword = function(){		
		return $scope.resetRequest.password.$invalid 
			&& !$scope.resetRequest.password.$pristine;
	};
	
	$scope.invalidCPassword = function(){		
		return !$scope.resetRequest.cpassword.$pristine
			&& ($scope.password != $scope.cpassword);
	};
	
	$scope.requestReset = function(){
		
		if($scope.emailUsername != ''){
			$http.get(_lbUrls.creset, {
				params : { 
					u : $scope.emailUsername
				}
			})
			.then(
					function(response){
						var resp = response.data;
						if(resp.success){
							$scope.successNotification = 
								"We have email the password reset link to your registered email";							
						}
						else{
							$scope.pageLevelAlert = resp.message;
						}
						
					},
					function(){
						$scope.pageLevelAlert = $rootScope.errMsgContactSupport;
					}
				);
		}
		else{
			$scope.pageLevelAlert = "Please provide username or email to reset your password.";
		}
		
	};
	
	
	$scope.changePassword = function(){
		
		if($scope.password != '' && ($scope.password == $scope.cpassword)){
			
			$http.post(_lbUrls.rsavep, {
				username : $('#formusername').val(),
				email : $('#formemail').val(),
				password : $scope.password
			})
			.then(
					function(response){
						var resp = response.data;
						if(resp.success){
							$scope.successNotification = 
								"Password changed successfully";							
						}
						else{
							$scope.pageLevelAlert = resp.message;
						}
						
					},
					function(){
						$scope.pageLevelAlert = $rootScope.errMsgContactSupport;
					}
				);
		}
		else{
			
			if($scope.password == ''){
				$scope.pageLevelAlert = "Please provide valid passwords";
			}
			
			else if($scope.password != $scope.cpassword){
				$scope.pageLevelAlert = "Passwords don't match";
			}
		}
		
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
	var $img = $('.prdpage-img');
	$scope.zoomFn = {
			activate : function(){
				
				var url = $img.find('img').attr('data-zoom');
				if(url && url !=''){
					
					$img.zoom({
						url : url,
						callback: function(){
							$(this).parent().addClass('loaded');
						}
					});					
				}
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
	$scope.orderMin = 9999;
	$scope.config = {};

	$scope.couponApplied = true;  //Only refers to coupons
	$scope.offersApplied = false; //Refers to all kinds of offers
	$scope.emptyCart = true;
	$scope.goodToProceed = false;
	
	if(_globalUserId) $scope.user._id = _globalUserId;
	
	
	$scope.cartLoadSuccess = function(resp){
		
		$scope.cartLoading = false;
		$scope.order = resp.data.order;
		
		/*If we have customer info, set it to proper scope variable*/
		$scope.user = resp.data.user?resp.data.user:{};
		
		/*General Control configurations*/
		if(resp.data.config){			
			$scope.config = resp.data.config;
			if($scope.config.orderMinimum && $scope.config.orderMinimum > 0){				
				$scope.orderMin = $scope.config.orderMinimum;
			}
		}
		
		/*if there is billing info in the order, use it*/
		if($scope.order && $scope.order.billing){				
			$scope.user.billing = $scope.order.billing;
			$scope.addressLogic();
		}
		
		else if($scope.user && $scope.user.billing){
			$scope.cartAddress.$setDirty();
		}		

		/*Perform this atlast*/
		if($scope.order){ 
			$scope.processOrder();
		}
	};
	
	
	$scope.getCart = function(){
		$http.get(_lbUrls.getcart, {
			params : { 
				'hidepop' : true  //tells the config not to show the loading popup
			}
		})
		.then($scope.cartLoadSuccess, function(){$scope.cartLoading = false;});
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
							$scope.processOrder();
							
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
			.then(function(resp){
				if(resp.data && resp.data.success){
					
					$rootScope.rootCartCount = resp.data.cartCount;
					$scope.order = resp.data.order;
					$scope.processOrder();
					
					_lbFns.pSuccess('Order Updated.');		
				}
				else{
					$scope.pageLevelAlert = resp.data.message;
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
							
							$scope.order = resp.data.order;
							$scope.processOrder();
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
							
							$scope.order = resp.data.order;
							$scope.processOrder();
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
	
	
	/* When ever there is a change in $scope.order, this function needs to be called 
	 * most of the logic control flags are set here. */
	$scope.processOrder = function(){
		
		$scope.sales = [];		
		
		var couponApplied = false,
			offersApplied = false,
			goodToProceed = false,
			emptyCart = true;
		
		
		if($scope.order.lineItems && $scope.order.lineItems.length>0){
			$scope.order.lineItems.forEach(function(item){
				
				if(item.instock){
					
					if(item.type=='item'){					
						emptyCart = false;
					}
					
					
					if(item.type=='coupon'){					
						couponApplied = true;
					}
					
					
					if(item.promo && item.promo != ''){					
						offersApplied = true;
					}
					
					
					
					if(item.type=='item' 
						&& (item.promo == 's' || item.promo == 'doubledownoffer')){
						
						var productName = item.name.length>15?item.name.substr(0,15)+'...':item.name,
						discount = (item.cost - item.price)*item.qty;
						
						
						if(item.promo == 'doubledownoffer')
							productName = 'Double Down Offer';
							
						
						if(!isNaN(discount) && discount > 0){						
							$scope.sales.push({
								'name' : productName,
								'price' : discount
							});
						}
						
					}
				}
			})
		}
		
		goodToProceed = $scope.order 
							&& ($scope.order.total > $scope.orderMin)
							&& !emptyCart 
							&& $scope.addressValidated;
		
		
		
		$scope.couponApplied = couponApplied;
		$scope.offersApplied = offersApplied;
		$scope.emptyCart = emptyCart;
		$scope.goodToProceed = goodToProceed;
		
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
};

lbApp
//below fns are defined in angular-general-functions.js
.config(appConfigs)
.run(appRunFns) 
//end fns defined in angular-general-functions.js


.controller('allProductCtrlr', allProductCtrlr)
.controller('categoryCtrlr', categoryCtrlr)
.controller('productCtrlr', productCtrlr)
.controller('loginCtrlr', loginCtrlr)
.controller('registerCtrlr', registerCtrlr)
.controller('resetCtlr', resetCtlr)
.controller('cartCtrlr', cartCtrlr);