var lbApp = angular.module(
	'lbApp', [
	     //'ngRoute',
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
allProductCtrlr = function($scope, $rootScope, $http, $templateRequest, $compile){

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
		
		if($scope.currentPid != 0){	
			
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
},

allProductPriceCtrlr = function($scope, $rootScope, $http, $timeout){

	$scope.itemTotal = 0;
	$scope.calculateItemTotal = function(){
		$scope.itemTotal = 0;
		//Set the quantity as zero;
		for(var i=0; i<$scope.prices.length; i++){
			var curr = $scope.prices[i];
			if(curr.qty != 0){
				if(curr.salePrice){
					$scope.itemTotal+=(curr.salePrice*curr.qty);
				}
				else{
					$scope.itemTotal+=(curr.regPrice*curr.qty);
				}
			};
		}
	};
	
	$scope.itemError = '';
	$scope.itemSuccess = '';
	$scope.quickAddToCart = function(event){
		
		$scope.itemError = '';
		$scope.itemSuccess = '';
		
		var $angularElement = angular.element(event.target).closest('li'),
		
		productName = $angularElement.data('pname'),
		productId = $angularElement.data('pid'),
		featuredImg = $angularElement.data('img'),
		
		lineItems = [];		
		
		for(var i=0; i<$scope.prices.length; i++){
			var curr = $scope.prices[i];
			if(curr.qty != 0){

				lineItems.push({
					name : productName,
					qty : curr.qty,
					productId : productId,
					img : curr.img ? curr.img : featuredImg,
					variationId : curr._id ? curr._id : 0
				});
			};
		}
			
		if(lineItems.length==0){
			$scope.itemError = 'Please select quantity for atleast one item.';
			return false;
		}

		$scope.addingToCart = true;
		
		//Reset Messages
		$scope.itemError = '';
		$scope.itemSuccess = '';
		$scope.$parent.setPageAlert('');
		
		$http.post(_lbUrls.addtocart+'/multi/?hidepop', lineItems)
		.then(function(resp){
			
			if(resp.data && resp.data.success){
				
				$scope.productInCart = resp.data.productCount;
				$rootScope.rootCartCount = resp.data.cartCount;
				$.cookie('lbbagnumber', resp.data.orderId, {expires:30, path:'/'});
				$scope.itemSuccess = 'Item(s) added to cart.';
				
				$timeout(function () {
					$scope.itemSuccess = "";
			    }, 4000);
				
				//Reset Quantity
				for(var i=0; i<$scope.prices.length; i++){
					$scope.prices[i].qty = 0;
				}
				
				//Reset itemTotal
				$scope.itemTotal = 0;
				
				//Send signal for sidecart load;
				$rootScope.$broadcast('mCartReload');
			}
			else {
				$scope.itemError  = resp.data.message;
			}

			$scope.addingToCart = false;
		},
		function(resp){
			if(resp.status == 403){
				$scope.$parent.setPageAlert("Your browser was idle for long. "
					+"Please refresh the page and add the item to cart again.");
			}
			else {
				$scope.$parent.setPageAlert("There was some error creating the order. "
					+"Please try later. If problem persists, please call the customer care.");
			}

			$scope.addingToCart = false;
		});		
		
		
	};
	
	var scopeInit = function(){
		
		$scope.prices = $scope.$parent.prices;
		
		//Sort the data
		$scope.prices.sort(function(a,b){return a.regPrice - b.regPrice;});
		
		var itemsOutofStock = true;
		//Check stock status and Set the quantity as zero;
		for(var i=0; i<$scope.prices.length; i++){
			$scope.prices[i].qty = 0;
			
			if($scope.prices[i].stockStat=='instock')
				itemsOutofStock =false;
		}
		
		$scope.itemsOutofStock = itemsOutofStock;

		//save price for future uses.
		$scope.$parent.productPrices[$scope.$parent.currentPid] = $scope.prices;
		
		//calculate new total
		$scope.calculateItemTotal();
	};
	
	scopeInit();
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
					
					location.href = "/pending-registration";
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
								"We have emailed the password reset link to your registered email address";							
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

cartMainCtrlr = function($scope, $http, $templateRequest, $compile){
	var m = this,
	deliveryLoaded = false;
	
	m.order = {};	
	m.ddprds = [];
	m.user = {};
	m.orderMin = 9999;
	m.config = {};
	m.sales = [];
	
	m.cartPage = false;
	m.cartLoading = true;
	m.pmtCOD = false;
	
	m.cartData = null;
	m.ccErrors = '';

	$scope.pageLevelAlert='';
	
	if(_globalUserId) m.user._id = _globalUserId;
	
	
	
	m.couponApplied = true;  //Only refers to coupons
	m.offersApplied = false; //Refers to all kinds of offers
	m.orderAboveOrderMin = false;
	m.emptyCart = true;
	m.addressSaved = false;
	m.codEnabled = true;
	m.ccPmtEnabled = true;
	
	m.doubleDownActive = false;
	m.doubleDownApplied = false;
	m.doubleDownEligible = false;
	
	
	/* When ever there is a change in m.order, this function needs to be called 
	 * most of the logic control flags are set here. */
	m.processOrder = function(){
		
		m.sales = [];		
		
		var couponApplied = false,
			offersApplied = false,
			doubleDownApplied = false,
			emptyCart = true;
		
		
		if(m.order.lineItems && m.order.lineItems.length>0){
			m.order.lineItems.forEach(function(item){
				
				if(item.instock){
					
					if(item.type=='item'){					
						emptyCart = false;
						
						if(item.img){
							item.img = item.img.replace('.jpg','-150x150.jpg');
						}
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
						
						
						if(item.promo == 'doubledownoffer'){
							productName = 'Double Down Offer';
							doubleDownApplied = true;
						}
						
							
						
						if(!isNaN(discount) && discount > 0){						
							m.sales.push({
								'name' : productName,
								'price' : discount
							});
						}
						
					}
				}
			})
		}	
		
		m.doubleDownApplied = doubleDownApplied;
		m.couponApplied = couponApplied;
		m.offersApplied = offersApplied;
		m.emptyCart = emptyCart;
		
		
		//OrderMin Check
		if(m.order.total >= m.orderMin){
			m.orderAboveOrderMin = true;
			
			if(m.cartPage && !deliveryLoaded) m.loadDeliveryTemplate();			
		} 
		else {			
			m.orderAboveOrderMin = false;
			
			if(m.cartPage) m.destroyDeliveryTemplate();
		}
		
		
		//DoubleDown Check
		if(m.doubleDownActive 
				&& m.order.total >= m.config.doubleDown){
			m.doubleDownEligible = true;
		}
		else {
			m.doubleDownEligible = false;
		}		
		
		
		if(m.cartPage && m.emptyCart){			
			//Will destory item, coupon, delivery, payment and review (if exists)!
			m.destroyItemTemplate();
		}
		
	};
	
	
	m.getCart = function(){
		$http.get(_lbUrls.getcart, {
			params : { 
				'hidepop' : true  //tells the config not to show the loading popup
			}
		})
		.then(
				//GetCartSuccess
				function(resp){
			
					m.cartLoading = false;
					m.order = resp.data.order;
					
					/*If we have customer info, set it to proper scope variable*/
					m.user = resp.data.user?resp.data.user:{};
					
					/*General Control configurations*/
					if(resp.data.config){			
						m.config = resp.data.config;
						if(m.config.orderMinimum && m.config.orderMinimum > 0){				
							m.orderMin = m.config.orderMinimum;
						}
						
						/* if we have a value > 0 for double down and we have 
						 * products for double down	*/
						if(m.config.doubleDown 
								&& m.config.doubleDown > 0 
								&& resp.data.ddPrds
								&& resp.data.ddPrds.length > 0){
							
							m.doubleDownActive = true;
							m.ddprds = resp.data.ddPrds;
						}
					}	
					

					if(m.order){ 
						m.processOrder();
					}
					
					
					if(m.cartPage && !m.emptyCart){				
						m.loadItemTemplate();						
						m.loadCouponTemplate();
					}
					else if(!m.emptyCart && !m.cartPage){
						$('body').removeClass('sidecartClose').addClass('sidecartOpen');
					}
					else if(m.emptyCart && !m.cartPage){
						if($('body').hasClass('.sidecartOpen'))
								$('body').removeClass('sidecartOpen').addClass('sidecartClose');
					}
				}, 
				
				//GetCartError
				function(){
					m.cartLoading = false;
				}
		);
	};
	
	//Check if loaded from cart page
	if(angular.element('.cartPage').size()>0){
		m.cartPage = true;
	}	
	m.getCart();
	
	//Trigger cart load for sidecart from allProductPriceCtrlr
	$scope.$on('mCartReload', function(){
		m.getCart();
	});
		
	
	//Load item template
	m.loadItemTemplate = function(){
		
		$scope.ivm = $scope.$new();
				
		$templateRequest("/resources/ng-templates/cart/item.html")
		.then(function(html){
		      var template = angular.element(html);
		      angular.element('#itemCtrlr')
		      .append(template);
		      
		      $compile(template)($scope.ivm);
		 });
	};
	
	//destroy item template
	m.destroyItemTemplate = function(){		
		if($scope.ivm){
			$scope.ivm.$destroy();
		}
		angular.element('#itemCtrlr').empty();
		
		//destroy other templates as well (cascaded action)
		m.destroyDeliveryTemplate();
		m.destroyCouponTemplate();
	};
		
	
	
	
	
	//Load coupon template
	m.loadCouponTemplate = function(){
		
		$scope.cvm = $scope.$new();
			
		$templateRequest("/resources/ng-templates/cart/coupon.html")
		.then(function(html){
		      var template = angular.element(html);
		      angular.element('#couponCtrlr')
		      .append(template);
		      
		      $compile(template)($scope.cvm);
		 });
	};
	
	//destroy coupon template
	m.destroyCouponTemplate = function(){		
		if($scope.cvm){
			$scope.cvm.$destroy();
		}
		angular.element('#couponCtrlr').empty();
	};
		
	
	
	
	
	//Load delivery template
	m.loadDeliveryTemplate = function(){
		
		$scope.dvm = $scope.$new();
				
		$templateRequest("/resources/ng-templates/cart/delivery.html")
		.then(function(html){
		      var template = angular.element(html);
		      angular.element('#deliveryCtrlr')
		      .append(template);
		      
		      $compile(template)($scope.dvm);
		      
		      deliveryLoaded = true;
		 });
	};
	
	//destroy delivery template 
	m.destroyDeliveryTemplate = function(){
		if($scope.dvm){
			$scope.dvm.$destroy();
		}		
		angular.element('#deliveryCtrlr').empty();
	    deliveryLoaded = false;
		
		//Destroy payment 
		m.destroyPaymentTemplate();
		
		//Destroy related 'm' fields
		m.addressSaved = false;
	};
	
	
	
	
	//Load payment template
	m.loadPaymentTemplate = function(){
		
		$scope.pvm = $scope.$new();
			
		$templateRequest("/resources/ng-templates/cart/payment.html")
		.then(function(html){
		      var template = angular.element(html);
		      angular.element('#paymentCtrlr')
		      .append(template);
		      
		      $compile(template)($scope.pvm);
		 });
	};	
	
	//destroy payment template
	m.destroyPaymentTemplate = function(){		
		if($scope.pvm){
			$scope.pvm.$destroy();
		}
		angular.element('#paymentCtrlr').empty();
		
		//Destroy paymentForm and related 'm' fields
		try{if(m.paymentForm) m.paymentForm.destroy();}catch(e){console.log(e);}
		m.cardData = null;
		m.ccErrors = '';
		
		//Destroy review as well;
		m.destroyReviewTemplate();
	};
		
	
	
	
	
	//Load review template
	m.loadReviewTemplate = function(){
		
		$scope.rvm = $scope.$new();
				
		$templateRequest("/resources/ng-templates/cart/review.html")
		.then(function(html){
		      var template = angular.element(html);
		      angular.element('#reviewCtrlr')
		      .append(template);
		      
		      $compile(template)($scope.rvm);
		      
		      /* Scroll down a bit, so customer can see next step*/
		      $('html,body').animate({scrollTop:$('body').scrollTop() + 150},500);
		 });
	};
	
	//destroy review template
	m.destroyReviewTemplate = function(){		
		if($scope.rvm){
			$scope.rvm.$destroy();
		}
		angular.element('#reviewCtrlr').empty();
	};
	
},

cartItemCtrlr = function($scope, $http, $rootScope, $timeout){

	$scope.itsOffHour = false;
	
	var m = $scope.m,
	
	checkOffHours = function(){
		
		if(m.emptyCart){
			console.log('checkoffhours cancelled');
			return;
		}
		
		var currHour = (new Date()).getHours();		
		if(currHour >= 23 || currHour < 11){ 
			$scope.itsOffHour = true;
		}  
		else{
			$scope.itsOffHour = false;
		}
		
		$timeout(checkOffHours, 1000 * 60); //check everyminute
	};	
	checkOffHours(); //run for the first time;
	
	$scope.itemRemove = function(){
		
		$scope.$parent.$parent.pageLevelAlert='';
		
		if(m.order && this.item){
			var req = {
					method: 'DELETE',
					url: _lbUrls.cart + 'removeitem',
					params: {'oid' : m.order._id, 
						'pid' : this.item.productId,
						'vid' : this.item.variationId
					}
			};
			
			$http(req)
			.then(
					function(resp){
						if(resp.data && resp.data.success){
					
							$rootScope.rootCartCount = resp.data.cartCount;
							
							m.order = resp.data.order;
							m.processOrder();
							
							if(m.cartPage) _lbFns.pSuccess('Item removed.');
						}
					
						else if(resp.data && resp.data.message){
							if(m.cartPage) $scope.$parent.$parent.pageLevelAlert = resp.data.message;
						}
						else{
							if(m.cartPage) $scope.$parent.$parent.pageLevelAlert = $rootScope.errMsgPageRefresh;
						}
					},
					
					function(){
						if(m.cartPage) $scope.$parent.$parent.pageLevelAlert = $rootScope.errMsgPageRefresh;
					}
			);
		}
		else {
			if(m.cartPage) $scope.$parent.$parent.pageLevelAlert = $rootScope.errMsgPageRefresh;
		}
	};
	
	
	$scope.updateCart = function(per){
		
		$scope.$parent.$parent.pageLevelAlert = '';
		
		if(!m.cartPage && per){
			this.item.qty = this.item.qty + parseInt(per);
		}
		
		if(this.item && this.item.qty && this.item.qty !=0){
			$http.get(_lbUrls.cart + 'updatecart', {
				params : { 
					'oid' : m.order._id,
					'vid' : this.item.variationId,
					'pid' : this.item.productId,
					'qty' : this.item.qty
				}
			})
			.then(function(resp){
				if(resp.data && resp.data.success){
					
					$rootScope.rootCartCount = resp.data.cartCount;
					m.order = resp.data.order;
					m.processOrder();
					
					if(m.cartPage) _lbFns.pSuccess('Order Updated.');		
				}
				else{
					if(m.cartPage) $scope.$parent.$parent.pageLevelAlert = resp.data.message;
				}
			});
		}
		else if(!this.item.qty){
			$scope.$parent.$parent.pageLevelAlert = "Please provide a valid quantity";
			return false;
		}
		else {
			$scope.$parent.$parent.pageLevelAlert = $rootScope.errMsgPageRefresh;
		}
	};
	
	
	$scope.addDoubleDown = function(){
		
		$http.post(_lbUrls.cart + '/adddoubledown/' + this.prd._id)
		.then(function(resp){
			if(resp.data && resp.data.success){
				
				$rootScope.rootCartCount = resp.data.cartCount;
				
				m.order = resp.data.order;
				m.processOrder();
				
				_lbFns.pSuccess('Offer Item Added.');
			}
		
			else if(resp.data && resp.data.message){
				if(resp.data.message.indexOf('/product')>-1){
					location.href = resp.data.message;
				}
				else{
					$scope.$parent.$parent.pageLevelAlert = resp.data.message;
				}
			}
			else{
				$scope.$parent.$parent.pageLevelAlert = $rootScope.errMsgPageRefresh;
			}
		},
		function(resp){
			if(resp.status == 403){
				$scope.$parent.$parent.pageLevelAlert  = "Your browser was idle for long. "
					+"Please refresh the page and add the item to cart again.";
			}
			else {
				$scope.$parent.$parent.pageLevelAlert  = "There was some error adding the item. "
					+"Please try later. If problem persists, please call the customer care.";
			}
		});
	};	
	
	$scope.prdCartClose = function(){
		$('body').removeClass('sidecartOpen').addClass('sidecartClose');
	};
},

cartDeliveryCtrlr = function($scope, $http, $rootScope, $filter, $timeout){

	var m = $scope.m,
	addressApiInit = false,
	
	getAddress = function(){
		return m.order.shipping.address.address1 + ' ' + m.order.shipping.address.city + ' ' + 
				m.order.shipping.address.state + ' ' + m.order.shipping.address.zip;
	},	
	
	getAddressMapLink = function(){
		if(m.order.shipping && m.order.shipping.address) {
			var mapLink = {
					'markers':'color:blue|label:S|' + getAddress(),
					'zoom':17,
					'scale':2,
					'size':'280x280',
					'center':getAddress(),
					'key':'AIzaSyDOOuxNzzE247y4HbG9B5J2yM8vzzhegCU'};
			
			
			return "https://maps.googleapis.com/maps/api/staticmap?" 
						+ $.param(mapLink);
		}
		else
			return null;
	},
	
	addressLogic = function(){
		
		if(m.order.shipping.address && (m.order.shipping.address.address1 != '' 
			|| m.order.shipping.address.city != ''
			|| m.order.shipping.address.state != ''
			|| m.order.shipping.address.zip != '')){			 
			
			$scope.mainZip = m.order.shipping.address.zip;
			$scope.validateZip(true);
		}
	},
	
	proceedToPayment = function(){
		
		m.addressSaved = true; 
		
		$('.delivery-address').empty()
		.append($('<img />').attr({'src' : getAddressMapLink(), 'class':'fit'}));
		
/*		if($scope.shippingSelected) 
			m.codEnabled = false;
		else*/
			m.codEnabled = true;
		
		if(m.user && m.user._id)
			m.loadPaymentTemplate();
	},
	
	setUpDeliveryTimes = function(){
		
		if(m.emptyCart){
			console.log('setUpDeliveryTimes cancelled');
			return;
		}
		
		var d = new Date(),
		currHour = d.getHours(),
		timeTocheck = 1000 * 60 * 60; //default every hour	
		
		if(currHour >= 23){ //if past 11PM
			//delivered tomorrow after 11 AM;
			d.setDate(d.getDate()+1);
			
			$scope.deliveryTimeText = 'Tomorrow - ' 
				+ $filter('date')(d, 'mediumDate') 
				+ ' after 11:00 AM PST';			
			
			$scope.endTime = null;		
		}  
		
		else if(currHour < 11){//if before 11AM, delivered today after 11 AM;
			
			if(currHour > 10){//After 10 AM			
				if(d.getMinutes() < 30){
					//start checking every 15 minute.
					timeTocheck = 1000 * 60 * 15;
				}
				else {
					//start checking every minute.
					timeTocheck = 1000 * 60;
				}
			}
			
			
			$scope.deliveryTimeText = 'Today - ' 
				+ $filter('date')(d, 'mediumDate') 
				+ ' after 11:00 AM PST';		
			
			$scope.endTime = null;
		}
		
		else if(currHour > 20){//During our operational period
			
			if(d.getMinutes() < 30){
				//start checking every 15 minute.
				timeTocheck = 1000 * 60 * 15;
			}
			else {
				//start checking every minute.
				timeTocheck = 1000 * 60;
			}
			
			
			d.setHours(22,59,0);			
			$scope.endTime = d;
			
			$scope.deliveryTimeText = 'Today - ' 
				+ $filter('date')(d, 'mediumDate');
		}
		else{
			//delivered today
			
			$scope.deliveryTimeText = 'Today - ' 
				+ $filter('date')(d, 'mediumDate');		
			
			$scope.endTime = null;
		}
		
		$timeout(setUpDeliveryTimes, timeTocheck);
	},
	
	initAddressVars = function(){
		if(!m.order.shipping) m.order.shipping = {};		
		if(!m.order.shipping.address) m.order.shipping.address = {};
	};	
	

	$scope.addressError = '';
	$scope.deliveryOptionSelected = true; //false;
	$scope.deliveryEligible = false;
	$scope.shippingEligible = false;
	$scope.deliverySelected = true; //false;
	$scope.shippingSelected = false;
		
	$scope.deliveryTimeText = '';
	$scope.endTime = null;
	
	
	

	$scope.resetDeliveryOptions = function(){
/*		$scope.deliveryEligible = false;
		$scope.shippingEligible = false;
		
		$scope.deliverySelected = false;
		$scope.shippingSelected = false;

		$scope.deliveryOptionSelected = false;*/
		
		$scope.addressError = '';
		$scope.invalidZip = '';
		
		m.addressSaved = false;
		
		/** Destroy payment form is present **/
		m.destroyPaymentTemplate();
	};	
	
	
	$scope.checkDeliveryOptions = function(){
		$scope.validateZip();
		
		if(m.order.shipping && m.order.shipping.address){
			m.order.shipping.address.zip = $scope.mainZip;
		}
	};
	
	$scope.invalidZip = '';
	$scope.validateZip = function(auto){
		
		$scope.resetDeliveryOptions();
		
		$http.get(_lbUrls.cart + '/validatezip',{
			params : { 
				'zip' : $scope.mainZip
			}
		})
		.then(
				function(resp){
					if(resp.data && resp.data.success){
						if(resp.data.message == "both"){
							$scope.deliveryEligible = true;
							$scope.shippingEligible = true;
							
							//If both option available, select delivery in auto mode.
							if(auto){
								$scope.deliverySelected = true;
								$scope.selectDeliveryOption();
							}
						}
						else if(resp.data.message == "local"){
							$scope.deliveryEligible = true;		
							
							if(auto){
								$scope.deliverySelected = true;
								$scope.selectDeliveryOption();
							}
						}
						else if(resp.data.message == "shipping"){
							$scope.shippingEligible = true;		
							
							if(auto){
								$scope.shippingSelected = true;
								$scope.selectDeliveryOption();
							}					
						}
						else if(!auto){
							$scope.invalidZip = "Sorry, we currently don't service your area. " 
								+ "We are working very hard on expanding to your city. ";								
						}
					}
				},
				
				function(){
					$scope.invalidZip = $rootScope.errMsgPageRefresh;
				}
		);
	};
	
	
	$scope.selectDeliveryOption = function(){
		$scope.deliveryOptionSelected = true;

		if(!addressApiInit)
			$scope.mapsInit();
	};
	
	
	$scope.saveDeliveryAddress = function(){
		
		if($scope.cartAddress.$valid){
			$scope.addressError = '';
			
			if($scope.deliverySelected) 
				m.order.shipping.deliveryMethod = 'Local Delivery';
			
			else if($scope.shippingSelected)
				m.order.shipping.deliveryMethod = 'Overnight Shipping';
			
			$http.post(_lbUrls.cart + m.order._id + '/savedeliveryaddr', m.order.shipping)
			.then(
					function(resp){
						if(resp.data && resp.data.success){
							_lbFns.pSuccess('Address Saved.');
							
							if(resp.data.message){
								m.order.shipping.deliveryMethod = resp.data.message;
								
								if(resp.data.message == 'Local Delivery'){
									$scope.deliverySelected = true;
									$scope.shippingSelected = false;
								}
								
								else if(resp.data.message == 'Overnight Shipping'){
									$scope.shippingSelected = true;
									$scope.deliverySelected = false;
								}
							}
							
							proceedToPayment();
						}
					
						else if(resp.data && resp.data.message){
							$scope.addressError = resp.data.message;
						}
						else{
							$scope.addressError = $rootScope.errMsgPageRefresh;
						}
					},
					
					function(){
						$scope.addressError = $rootScope.errMsgPageRefresh;
					}
			);			
		}		
	};	
	
	
	$scope.editAddress = function(){
		m.addressSaved = false;
		
		$scope.showPayment = false;
		$scope.cartAddress.$setDirty();
		
		if(!addressApiInit)
			$scope.mapsInit();
		
		/** Destroy payment form is present **/
		m.destroyPaymentTemplate();
	};
	
	
	$scope.saveDeliveryNotes = function(){		
		
		if(!m.order.notes) m.order.notes = {};
		if(!m.order.notes.deliveryNotes) m.order.notes.deliveryNotes = '';
		
		if(m.order._id && m.order.notes.deliveryNotes){			
			$http.post(_lbUrls.cart + m.order._id + '/savedeliverynote?hidepop',
				{'deliveryNotes':m.order.notes.deliveryNotes});			
		}		
	};			
	
	
	/* Google MAPS API related Functions */
	$scope.gmapComponentForm = {
	        street_number: 'short_name',
	        route: 'short_name', //Street Address
	        locality: 'long_name',  //city
	        administrative_area_level_1: 'short_name',  //State
	        postal_code: 'short_name'
	};
	
	$scope.geolocate = function() {
		if (navigator.geolocation) {
			navigator.geolocation.getCurrentPosition(function(position) {
				var geolocation = {
						lat: position.coords.latitude,
						lng: position.coords.longitude
				};
				var circle = new google.maps.Circle({
					center: geolocation,
					radius: position.coords.accuracy
				});
				
				$scope.autocomplete.setBounds(circle.getBounds());
			});
		}
	};
	
	$scope.fillInAddress = function(){
		var place = $scope.autocomplete.getPlace(),
		streetNumber = '',
		streetAddr = '';
		
		for (var i = 0; i < place.address_components.length; i++) {
			var addressType = place.address_components[i].types[0];
			if ($scope.gmapComponentForm[addressType]) {
				var val = place.address_components[i][$scope.gmapComponentForm[addressType]];
				
				switch(addressType){
				
					case 'street_number':
						streetNumber = val; 
						break;
						
					case 'route':
						streetAddr = val; 
						break;
					
					case 'locality':
						m.order.shipping.address.city = val; 
						break;
					
					case 'administrative_area_level_1':
						m.order.shipping.address.state = val; 
						break;
					
					case 'postal_code':
						m.order.shipping.address.zip = val; 
						break;
						
					default:
						break;
				}
			}
		}
		
		m.order.shipping.address.address1 = streetNumber? (streetNumber + ' ' + streetAddr) : streetAddr;
		try{$scope.$apply();}catch(err){}
		
		$('#cartAddress .form-control').each(function(){
			$(this).blur();
		});
	};
	
	$scope.mapsInit = function(){
		$scope.autocomplete = new google.maps.places.Autocomplete(
				(document.getElementById('addressLocator')),
	            {
					types: ['geocode'],
					componentRestrictions: {country: 'us'}
				});
		
		$scope.autocomplete.addListener('place_changed', $scope.fillInAddress);
		$scope.geolocate();
		
		initAddressVars();
		
		addressApiInit = true;
	};	
	
	/* Google MAPS API related Functions Ends */
	

	
	/** DELIVERY INIT **/	
	(function(){
		
		/*if there is shipping info in the order, use it*/
		if(m.order && m.order.shipping && m.order.shipping.address){		
			setUpDeliveryTimes();
			addressLogic();
			
			if(!addressApiInit)
				$scope.mapsInit();
		}
		
		else if(m.user && m.user.billing){		
			setUpDeliveryTimes();
			initAddressVars();
			
			m.order.shipping.address = m.user.billing;
			
			if(!addressApiInit)
				$scope.mapsInit();
		}
	})();
},

cartPaymentCtrlr = function($scope, $http, $rootScope, $timeout){

	var m = $scope.m,
	pmtApiInit = false;
	
	$scope.nonceRequesting = false;
	$scope.pmtChangable = true;
	$scope.pmtMethod = null;
	$scope.reviewLoaded = false;
	
	/** Payment form related fns **/
	m.cardNonceResponseReceived = function(errors, nonce, cardData) {
		
		if (errors) {			
			m.ccErrors = '';
			
			errors.forEach(function(error) {	
    			m.ccErrors+= error.message;
			});
			
		} else {			
			m.cardData = cardData;
			m.cardData.nonce = nonce;
			//console.log(m.cardData);
			
			$scope.proceedToReview();
		}
		
		$scope.nonceRequesting = false;
		try{$scope.$apply();}catch(err){}		
	};
	
	m.unsupportedBrowserDetected = function() {
		m.ccErrors = 'Sorry, Your browser doesn\'t support '
			+'the secure processing of this credit card. Please '
			+'try a different browser.';
		
		$scope.nonceRequesting = false;
		try{$scope.$apply();}catch(err){}
	};
	
	m.paymentForm = new SqPaymentForm({
		
		    applicationId: 'sq0idp-uKKHToPW2VxvmD6WKutvHA',
		    inputClass: 'form-control',
		    inputStyles: [
		      {
		        fontSize: '14px',
		        color: '#555'
		      }
		    ],
		    cardNumber: {
		      elementId: 'sq-card-number',
		      placeholder: '---- ---- ---- ----'
		    },
		    cvv: {
		      elementId: 'sq-cvv',
		      placeholder: 'CVV'
		    },
		    expirationDate: {
		      elementId: 'sq-expiration-date',
		      placeholder: 'MM/YY'
		    },
		    postalCode: {
		      elementId: 'sq-postal-code'
		    },
		    
		    callbacks: {
		    	cardNonceResponseReceived: m.cardNonceResponseReceived,		      
		    	unsupportedBrowserDetected: m.unsupportedBrowserDetected,
		      
		    	inputEventReceived: function(inputEvent) {
		    		switch (inputEvent.eventType) {
		    		
		    		case 'focusClassAdded':
		            // Handle as desired
		    			break;
		    		case 'focusClassRemoved':
		            // Handle as desired
		    			break;
		    		case 'errorClassAdded':
		            // Handle as desired
		    			break;
		    		case 'errorClassRemoved':
		            // Handle as desired
		    			break;
		    		case 'cardBrandChanged':
		            // Handle as desired
		    			break;
		    		case 'postalCodeChanged':
		            // Handle as desired
		    			break;
		    		}
		    	}
		    }
	 });
	
	 $scope.reqCardNonce = function() {	
		 m.ccErrors = '';
		 m.cardData = null;
		 $scope.nonceRequesting = true;
		 m.paymentForm.requestCardNonce();
	 };
	 /** Payment form related fns end **/
	 
	 

	$scope.squareUpInit = function(){
		if(!pmtApiInit) {			
			try{m.paymentForm.build();}catch(e){console.log(e);}
			
			$timeout(function(){
				m.paymentForm.setPostalCode(m.order.shipping.address.zip);
			}, 1000);
			
			pmtApiInit = true;
		}	
	};
	
	
	$scope.proceedToReview = function(){
		if($scope.pmtMethod == 'cod')
			m.pmtCOD = true;
		else
			m.pmtCOD = false;
		
		/* UPDATE THE PAYMENT PARAMS */
		if(!m.order.billing) m.order.billing = {};
		if(!m.order.billing.pmtMethod) m.order.billing.pmtMethod = {};
		
		m.order.billing.pmtMethod.method = $scope.pmtMethod;
		if($scope.pmtMethod=='cc')
			m.order.billing.pmtMethod.type = 'Credit Card';
		
		else if($scope.pmtMethod=='cod')
			m.order.billing.pmtMethod.type = 'Donation on Delivery';
		
		m.order.billing.pmtMethod.cardData = null;
		
		if(m.cardData){				
			m.order.billing.pmtMethod.cardData = m.cardData;
		}		
		/* UPDATE THE PAYMENT PARAMS ENDS */
		
		m.loadReviewTemplate();
		$scope.reviewLoaded = true;
	};
	
	
	$scope.clearReview = function(){
		m.destroyReviewTemplate();
		$scope.reviewLoaded = false;
	};
	 
	
	/* Pmt init function */
	(function(){
		if(!m.codEnabled) {
			$scope.pmtMethod = 'cc';
			$scope.pmtChangable = false;
			$scope.squareUpInit();
		}
		
		else if(!m.ccPmtEnabled) {
			$scope.pmtMethod = 'cod';
			$scope.pmtChangable = false;
		}
	})();
},

cartCouponCtrlr = function($scope, $http, $rootScope){
	
	var m = $scope.m;
	
	$scope.couponErrors = '';
	
	$scope.removeCoupon = function(){

		$scope.couponErrors = '';
		var couponCode = this.item.name;

		if(couponCode){
			$http.get(_lbUrls.cart + 'removecoupon/' + couponCode,{
				params : {
					'hidepop' : true,
					'oid' : m.order._id
				}
			})
			.then(
					function(resp){
						if(resp.data && resp.data.success){
							_lbFns.pSuccess('Promocode removed');

							m.order = resp.data.order;
							m.processOrder();
						}

						else if(resp.data && resp.data.message){
							$scope.couponErrors = resp.data.message;
						}
						else{
							$scope.couponErrors = $rootScope.errMsgPageRefresh;
						}
					},

					function(){
						$scope.couponErrors = $rootScope.errMsgPageRefresh;
					}
			);
		}
		else{
			$scope.couponErrors = "Invalid promo code";
		}

	}


	$scope.applyCoupon = function(){

		$scope.couponErrors = '';

		if($scope.couponCode){
			$http.get(_lbUrls.cart + 'applycoupon/' + $scope.couponCode,{
				params : {
					'hidepop' : true,
					'oid' : m.order._id
				}
			})
			.then(
					function(resp){
						if(resp.data && resp.data.success){
							_lbFns.pSuccess('Promocode applied');
							$scope.couponCode = '';

							m.order = resp.data.order;
							m.processOrder();
						}

						else if(resp.data && resp.data.message){
							$scope.couponErrors = resp.data.message;
						}
						else{
							$scope.couponErrors = 'Invalid promo code';
						}
					},

					function(){
						$scope.couponErrors = $rootScope.errMsgPageRefresh;
					}
			);
		}
		else{
			$scope.couponErrors = "Invalid promo code";
		}

	};
},

cartReviewCtrlr = function($scope, $http, $rootScope){
	
	var m = $scope.m;
	
	$scope.reviewErrors = '';
	$scope.placeOrder = function(){
		
		if(m.order._id && m.user._id){	
			
			m.ccErrors = '';
			$scope.reviewErrors = '';
			
			$http
			.post(_lbUrls.cart + m.order._id + '/placeorder', m.order)
			.then(
					function(resp){
						if(resp && resp.data){
							if(resp.data.success){
								location.href = "/confirmation/" + resp.data.message;
							}
							else{
								
								if(!resp.data.paymentProcessed && resp.data.paymentError != ''){
									
									if(resp.data.message == 'retry'){
										m.ccErrors = 'Payment request timed out, please try again.';
									}
									else{
										m.ccErrors = resp.data.paymentError;
									}

									m.cardData = '';
									m.destroyReviewTemplate();
								}
								else{
									
									if(resp.data.paymentProcessed && resp.data.orderFinalizationError){
										$scope.reviewErrors = 'Your payment was processed successfully. ' 
											+ 'But there was some error finializing the order. ' 
											+ 'Please don\'t place the order again. ' 
											+ 'Please call our customer care. Error details -  '+ resp.data.message;
									}
									
									else{
										$scope.reviewErrors = resp.data.message;										
									}
									
								}
							}
						}
						else{
							$scope.reviewErrors = 'There was error placing the order. '
								+'Please refresh the page and try again later. '
								+'If problem persists, please contact customer care.';
						}
					},
					function(){
						$scope.reviewErrors = $rootScope.errMsgPageRefresh;
					}
			);			
		}
		else{
			$scope.reviewErrors = 'Unable to find an order. '
				+'Are you sure you have items in your cart and '
				+'you are logged in to your account?';
		}
		
	};
},

localBoxCtrlr = function($scope, $http, $rootScope){
	
	$scope.quantity = 1;
	$scope.successMsg = '';
	$scope.errorMsg = '';
	$scope.addingToCart = false;
	
	$scope.addToCart = function(){
		$scope.errorMsg = '';
		$scope.successMsg = '';
		
		if(!$scope.quantity || $scope.quantity<=0){
			$scope.errorMsg  = 'Minimum quantity should be 1.';
			return false;
		}
		
		$scope.addingToCart = true;		
			
		$scope.lineItem = {
				name : 'Localbox',
				qty : $scope.quantity,
				productId : 10589,
				img : '/products/localbox.jpg',
				variationId : 0
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
};

lbApp
//below fns are defined in angular-general-functions.js
.config(appConfigs)
.run(appRunFns) 
//end fns defined in angular-general-functions.js


.controller('allProductCtrlr', allProductCtrlr)
	.controller('allProductPriceCtrlr',allProductPriceCtrlr)

.controller('productCtrlr', productCtrlr)
.controller('loginCtrlr', loginCtrlr)
.controller('registerCtrlr', registerCtrlr)
.controller('resetCtlr', resetCtlr)
.controller('cartMainCtrlr', cartMainCtrlr)
	.controller('cartItemCtrlr', cartItemCtrlr)
	.controller('cartDeliveryCtrlr', cartDeliveryCtrlr)
	.controller('cartPaymentCtrlr', cartPaymentCtrlr)
	.controller('cartCouponCtrlr', cartCouponCtrlr)
	.controller('cartReviewCtrlr', cartReviewCtrlr)
	
.controller('localBoxCtrlr', localBoxCtrlr)

.directive('remainingTime', remainingTime);