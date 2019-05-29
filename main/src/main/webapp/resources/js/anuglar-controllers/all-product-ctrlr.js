var allProductCtrlr = function($scope, $http, $rootScope, $templateRequest, $compile, categoryFltrFilter){

	$scope.prices = [];
	$scope.productPrices = {}; //this object is populated in allProductPriceCtrlr
	$scope.currentPid = 0;
	$scope._lbGlobalCDNPath = _lbGlobalCDNPath;
	
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
		
		if($scope.angularListOn && this.p){
			variation = this.p.variation;
			price = this.p.price;
			salePrice = this.p.salePrice;
			$scope.currentPid = this.p._id;			
		}
		
		if($scope.currentPid !== 0){	
			
			$scope.prices = [];

			if(!$scope.productPrices[$scope.currentPid]){				

				
				if(variation){
					
					//Variable product, get prices					
					$http.get(_lbUrls.getprd + $scope.currentPid + '/price', {
						params : { 
							'hidepop' : true  //tells the config not to show the loading popup
						}
					})
					.then(function(resp){
                                                var pID = resp.data[0].productId;                                               
                                                $scope.prices = resp.data;	
						$scope.getProDetailFromInvByID(pID);
						
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
	
        
        $scope.getProDetailFromInvByID = function (productId){
          
            var data = ''+productId;
           
            var config = {
                headers : {                   
                    'Authorization': 'Basic eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJuYW1lIjoiTWFpbiBBZG1pbiIsImlzcyI6ImF1dGgwIiwidXNlcnR5cGUiOiJvcGVyYXRvciIsInVzZXJuYW1lIjoiY2hhbXAifQ.muZ0ZV2SFNFnjgXhHhuk0iCM7klZ5PJIJq5U07QM8LQ'
                }
            };
            
            $http.post('/inventory/apps/acceptproductlist', productId, config)
            .success(function (data, status, headers, config) {
                $scope.prices.productDetail = {};
                if(data.success === true){
                    $scope.prices.productDetail= data.result[0];
                }
                console.log($scope.prices);
            }).error(function (data, status, header, config) {               
            });            
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
	
	
	/** Sort filter logic **/
	$scope.angularListOn = false;
	$scope.buildList = {
			catFilter : '',
			order : '',
			orderTxt : '',
			role : ''
	};
	
	$scope.orderAndOrderTxt = _lbConstants.productspage_orderAndOrderTxt; //defined in main.js
	$scope.sortOrderChange = function(order){
		$scope.buildList.order = order;
		$scope.buildList.orderTxt = $scope.orderAndOrderTxt[order];
	};
	
	//scan the page for all listing and build an array.
	var scanListing = function(){
		$scope.buildList.products = [];
		$scope.buildList.order = $('#productSortMenu').data('order') ? $('#productSortMenu').data('order') : '';
		$scope.buildList.orderTxt = $scope.orderAndOrderTxt[$scope.buildList.order];
		$scope.buildList.role = document.getElementById('lb_role_admin') ? 'admin' : '';
			
		$('#thymeleaf-productlist>li').each(function(index){
			var p = {};
			$e = $(this);		

                        //console.log('=====================');
                        console.log($e);
                        
                        p.name = $e.data("pname");
			p.stockStat = $e.data("stockstat");
			p._id = parseInt($e.data("pid"));
			p.featuredImg = $e.data("img");
			p.variation = $e.data("var");
			p.price = parseFloat($e.data("p"));
			p.salePrice = parseFloat($e.data("sp"));
			
			p.url = $e.data("url");
			p.categories = $e.data("cat");
			p.rating = parseInt($e.data("rating"));
			p.reviewCount = parseInt($e.data("review-count"));
			p.priceRange = $e.data("price-range");
			p.description = $e.data("desc");
			
			p.newBatchArrival   = $e.data("newest");
			p.minStockLimit     =  $e.data("minstocklimit");
			p.total_remain_qty  =  parseInt($e.data("total_remain_qty"));
                        
                        if(parseInt(p.total_remain_qty) < parseInt(p.minStockLimit)){ 
                           p.showProductLimitOption = true;
                        }else{
                           p.showProductLimitOption = null; 
                        }
                        
                        
			p.productFilters = {
                            price : parseFloat($e.data("filter-price")),
                            cbd : parseFloat($e.data("filter-cbd")), 
                            thc : parseFloat($e.data("filter-thc"))
			};
			
			//push product object to the list we made.
			$scope.buildList.products.push(p);
		});
		
		reBuildListing();
		//console.log($scope.buildList);
	},
	
	reBuildListing = function(){

		if(!$scope.buildList.products || $scope.buildList.products.length === 0) return;

		//console.log("Angular list build complete and off to template loading");
		
		//load productListTemplate		
		$templateRequest("productListTemplate")
		.then(function(html){
		      var template = angular.element(html);
		      angular.element('#angular-productlist')
		      	.empty()
		      	.append(template);
		      
		      $compile(template)($scope);
		      
		      $scope.angularListOn = true;
		      $('#thymeleaf-productlist').hide();
		      
		      $scope.productPrices = {}; //reset this object (populated in allProductPriceCtrlr)
		      
		 });
	};
	
	
	//Build angular productlist during initial load.
	scanListing();
},

categoryFilter = function(){
	
	return function(input, filter){
		
		if(!filter || $.trim(filter) === '')
			return input;
		
		var out = [];
		
		angular.forEach(input, function(product){
			
			if(product.categories && product.categories.indexOf(filter) > -1){
				out.push(product);
			}
			
		});
		
		return out;
	};
};