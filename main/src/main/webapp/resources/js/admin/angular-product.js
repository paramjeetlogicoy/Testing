//Create a random number for version control and avoid caching
var versionCtrl = '?v=' + Math.random();

var prdApp = angular.module(
	'productsApp', [
         'ngRoute',
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
defaultCtrlr = function($scope, $http, $filter, $sanitize){
	
	$scope.pageLevelError = '';
	$scope.pg = {};
	$scope.pg.currentPage = 1;
	
	$scope.sortType = '_id';
	$scope.sortReverse = true;
	$scope.productSearch = '';
	
        $scope.items = [];         
       
        $scope.getAllInventoryProduct = function () {
            //var data = $.param();
            var data;
        
            var config = {
                headers : {
                    'Authorization': 'Basic eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJuYW1lIjoiTWFpbiBBZG1pbiIsImlzcyI6ImF1dGgwIiwidXNlcnR5cGUiOiJvcGVyYXRvciIsInVzZXJuYW1lIjoiY2hhbXAifQ.muZ0ZV2SFNFnjgXhHhuk0iCM7klZ5PJIJq5U07QM8LQ'
                }
            };
            
            var url = '/inventory/apps/listallproducts';
            $http.post(url, data, config)
            .success(function (data, status, headers, config) {               
                if(data.success === true){
                   $scope.items = data.result;
                   $scope.getProducts();
                }                              
            });            
        };
        
        $scope.getAllInventoryProduct();
        $scope.selectProduct;
        $scope.selectedProduct = function (){
                $scope.ProductData = $scope.items.filter(function (detail) {                  
                    return (detail.id === $scope.pDetail);                    
                });
                
                if( $scope.ProductData.length > 0){                    
                    $scope.selectProduct = $scope.ProductData[0];                   
                }               
        };
        
        $scope.mappingProduct;
        $scope.showModel = function (mappingProduct){
          $scope.mappingProduct = mappingProduct;          
          $('#myModal').modal('show');
        };
        
        $scope.submitProduct = function (){            
            if($scope.pDetail !== undefined && $scope.pDetail !== null){
                
            var mongo_productid = $scope.mappingProduct._id;
            var data = new FormData();
            data.append("id", parseInt($scope.pDetail));
            data.append('mongo_productid', parseInt(mongo_productid));
            
            var config = {
                headers : {
                    'Authorization': 'Basic eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJuYW1lIjoiTWFpbiBBZG1pbiIsImlzcyI6ImF1dGgwIiwidXNlcnR5cGUiOiJvcGVyYXRvciIsInVzZXJuYW1lIjoiY2hhbXAifQ.muZ0ZV2SFNFnjgXhHhuk0iCM7klZ5PJIJq5U07QM8LQ',
                    'Content-Type' : undefined
                }
            };
            
            var url = '/inventory/apps/updateproductsmongoid';
            $http.post(url, data, config)
            .success(function (data, status, headers, config) {                  
                if(data.success === true){                      
                    $scope.getAllInventoryProduct();                   
                }  
                $("#myModal").modal("hide");
            });   
                
            }else{
                alert("Please choose product first.");
            }          
        };
        
	$scope.getProducts = function(){
		
		$http.get('/admin/products/json/', {
			params : { 
				'p' : $scope.pg.currentPage,
				'q' : $scope.productSearch,
				'o' : ($scope.sortReverse?'-':'') + $scope.sortType
			}

		}).success(function(data){
			if(data.success){				
                                var productList = [];                                                                
                                for(var i = 0; i < data.respData.length; i++){
                                  
                                    var mainproductRes = data.respData[i];
                                    mainproductRes['inventoryProductName'] = '';
                                    
                                     for(var j = 0; j < $scope.items.length; j++){
                                         var value_inv = $scope.items[j];
                                        // console.log(value_inv);
                                         if(value_inv.mongo_productid > 0){
                                            if (value_inv.mongo_productid === mainproductRes._id) {
                                                 mainproductRes['inventoryProductName'] = value_inv.productName;
                                            }
                                        }
                                     }                                   
                                    productList.push(mainproductRes);
                                }
                                           
                                $scope.products = productList;
				$scope.pg = data.pg
			}
			else if(data.message && data.message!=''){
				$scope.pageLevelError = data.message;
			}
			else{

				$scope.pageLevelError = "There was some error getting the products."
					+" Please contact G";
			}

		}).error(function(){
			$scope.pageLevelError = "There was some error getting the products."
					+" Please contact G";
		});	
		
	};
	
	$scope.pageChanged = function() {
		$scope.getProducts();
	};
	
	// $scope.getProducts(); // uncomment it after mapping of products.
},

prdCtrlrs = function($scope, $http, $filter, $routeParams, $location, mode, $sanitize, uploadService, $rootScope){
	
	$scope.params = $routeParams;
	$scope.errorMsg = '';
	$scope.p = {};
	$scope.prices = [];
	$scope.stockStats = ['instock','outofstock'];
	$scope.statuses = ['draft','publish','private','discontinued'];
	$scope.varValues = [];
	$scope.mode = mode; //Mode will be 'new' while adding a product, and 'edit', if editing a product.
	$scope.productId = $scope.params.productId;
	$scope.defaultAttr = -1;
	$scope.newBatchDate;
	
	/**UPLOAD SPECIFIC FUNCTIONS*/
	$scope.cdnPath = uploadService.cdnPath;
	uploadService.config.productUploader = true;
	
	$scope.afterSelect = function(){
		if(uploadService.selectedFiles){
			$scope.p.featuredImg = uploadService.selectedFiles[0].location;
			$scope.productDetailsForm.$setDirty();
		}
	};
	
	$scope.selectImage = function(){
		uploadService.config.cb = $scope.afterSelect;
		uploadService.config.fields = {path : '/products/'};
		uploadService.config.productUploader = true;
		uploadService.showGallery($rootScope);
	};
	
	$scope.setPriceImg = function(){
		uploadService.config.cb = $scope.afterPriceImgSelect;
		uploadService.config.currentObject = this.price._id;		
		uploadService.showGallery($rootScope);
	};
	
	$scope.afterPriceImgSelect = function(){
		if(uploadService.selectedFiles && uploadService.config.currentObject){
			var priceId = uploadService.config.currentObject;
			for(var i=0; i<$scope.prices.length; i++){
				if(priceId==$scope.prices[i]._id){
					$scope.prices[i].img = uploadService.selectedFiles[0].location;
					$scope.productPriceForm.$setDirty();
					break;
				}
			}
		}
	};
	
	
	$scope.selectVisualImg = function(){
		uploadService.config.cb = $scope.afterSelectVisualImg;
		uploadService.config.multipleSelect = true;
		uploadService.config.fields = {path : '/products/'};		
		uploadService.showGallery($rootScope);
	};
	
	$scope.afterSelectVisualImg = function(){
		if(uploadService.selectedFiles){
			
			if(!$scope.p.prdVisuals) {
				$scope.p.prdVisuals = [];
			}
			
			uploadService.selectedFiles.forEach(function(selectedFile){
				$scope.p.prdVisuals.push({
					'url': selectedFile.location,
					'type': 'image'
				});
			});
			
			$scope.productDetailsForm.$setDirty();
		}
	};
	
	
	$scope.selectVideoThumb = function(){
		uploadService.config.cb = $scope.afterSelectVideoThumb;
		uploadService.config.multipleSelect = true;
		uploadService.config.fields = {path : '/products/'};		
		uploadService.showGallery($rootScope);
	};
	
	$scope.afterSelectVideoThumb = function(){
		if(uploadService.selectedFiles){
			
			if(!$scope.p.prdVisuals) {
				$scope.p.prdVisuals = [];
			}
			
			uploadService.selectedFiles.forEach(function(selectedFile){
				$scope.p.prdVisuals.push({
					'thumbnail': selectedFile.location,
					'type': 'video'
				});
			});
			
			$scope.productDetailsForm.$setDirty();
		}
	};
	

	/**UPLOAD SPECIFIC FN ENDS*/
	
	//This is a initialized priceObject;
	$scope.priceLine = {"_id":0,"variationId":0,"productId":$scope.productId,
			"variation":[],"stockCount":1,"regPrice":0,"salePrice":0.0,
			"stockStat":"instock","image":null};
	
	$scope.getProductDetails = function($event){
		
		$http.get('/admin/products/json/' + $scope.productId)
		.success(function(data){
			$scope.p = data;			
			$('#product-editor').show(0, function(){
				$('.admin-editor-container')[0].scrollIntoView();
				$('[data-toggle="tooltip"]').tooltip();
			});
			
			//Remove millisecond part
			$scope.newBatchDate = new Date(Math.floor( $scope.p.newBatchArrival / (1000*60) )*1000*60)
			
			$scope.correctVarValues();
			
			if($scope.p.attrs){
				//Sort ascending on position
				$scope.p.attrs.sort(function(a,b){
					return (a.position-b.position);
				});
				
				$scope.getPrices();
			}
			
		}).error(function(){
			$scope.errorMsg = 'There was some error getting the product info. Please try later';
			$('#product-editor').show(0, function(){
				$('.admin-editor-container')[0].scrollIntoView();
			});
		});		
	};
	
	$scope.updateScopeDate = function(){
		$scope.p.newBatchArrival = $scope.newBatchDate.getTime();
		//console.log('$scope.p.newBatchArrival - ' + $scope.p.newBatchArrival);
	};
	
	$scope.getCurrentVarValues = function(attr){
		var values = [];
		$scope.varValues.forEach(function(e,i){
			if(e.attr.toLowerCase() == attr.toLowerCase()){
				values = e.values;
			}
		});
		//console.log("Values - " + values);
		return values;
	};
	
	
	$scope.correctVarValues = function(){

		$scope.varValues = [];
		if($scope.p.attrs){
			$scope.p.attrs.forEach(function(e, i){
				var attr = e.attr,
				values = e.values;
				$scope.varValues.push({"attr":attr,"values":values});
			});
		}
	};	
	
	$scope.closeProductDetails = function(){
		$scope.initalize();	
		$('#product-editor').hide();
	};
	
	
	$scope.noop = function(){};

	$scope.duplicateProduct = function(){
		$http.post('/admin/products/json/duplicateproduct/' + $scope.p._id)
		.then(function(resp){
			if(resp.data && resp.data.success){
				$location.path('/details/' + resp.data.message);
			}
			else{
				$scope.errorMsg = resp.data.message;
			}
		},function(){
			$scope.errorMsg  = "There was some error duplicating the product. "
				+"Please contact G";
		})
	};
	
	$scope.removePrdVisual = function(){
		var currItem = this.pVisual;
		for(var i=0; i<$scope.p.prdVisuals.length; i++){
			if($scope.p.prdVisuals[i] == currItem){
				$scope.p.prdVisuals.splice(i,1);
				$scope.productDetailsForm.$setDirty();
				
				break;
			}
		}
	};
	
	
	$scope.toDetails = function(){
		$('.product-pricing-holder').slideUp();
		$('.product-details-holder').slideDown();
	};
	
	$scope.saveProductGeneric = function(cb){	
		
		/**
		 * When mode is new, we need to generate productId in the first call to
		 * the server. Once Id is generated, we switch back to edit mode by
		 * calling the edit URL 
		 **/
		if($scope.mode == 'new'){
			
			$http.post('/admin/products/json/createproduct', $scope.p)
			.success(function(product){
				if(product._id && product._id != 0){
					$location.path('/details/' + product._id);
				}
				
				else if(product.description && product.description.length > 0){
					$scope.errorMsg = product.description;	
				}
				
				else{
					$scope.errorMsg  = "Error creating the product. "
						+"No valid productId returned. Please contact G";
				}
			})
			.error(function(){
				$scope.errorMsg  = "There was some error creating the product. "
						+"Please contact G";
			});
			
		}
		else{
			
			$http.post('/admin/products/json/savepdtls', $scope.p)
			.success(function(resp){
				$scope.reloadMemos();
				if(cb)
					cb(resp);
			})
			.error(function(){
				$scope.errorMsg  = "There was some error saving the product details. "
						+"Please contact G.";
			});			
		}
	};
	
	$scope.saveThenProceedToPricing = function(){
		$scope.saveDetails(true);
	}
	
	$scope.saveDetails = function(proceedToPricing){
		
		if($scope.productDetailsForm.$dirty){			
			$scope.saveProductGeneric(function(resp){
				if(resp.success){
					$scope.productDetailsForm.$setPristine();
					if(proceedToPricing === true)
						$scope.toPricing();		
					else
						_lbFns.pSuccess('product details saved');
				}
				else{
					$scope.errorMsg = resp.message;
					$('.admin-editor-container')[0].scrollIntoView();
				}
			});	
		}
		else
			$scope.toPricing();
	};
	
	/**
	 * On product name blur event, if the product URL is empty
	 * we will create URL based on the product name
	 **/
	$scope.createProductUrl = function(){		
		if($scope.p.name && $scope.p.name != '' 
				&& (!$scope.p.url || $scope.p.url == '')){
			
			//remove all non-alphabetic (except spaces) and then replace spaces with hypen
			$scope.p.url = $scope.p.name.toLowerCase().replace(/[^a-z0-9 ]/g, "").replace(/\s+/g, "-");
		}
	};
	
	
	$scope.toPricing = function(){
		$('.product-details-holder').slideUp();
		$('.product-pricing-holder').slideDown(function(){
			$('.product-attr-holder').slideDown();
			$('.product-price-holder').slideUp();
		});
	};
	
	$scope.attrCollapse = function(){
		var elem = event.target;
		if(elem.nodeName == 'I')
			elem =  elem.parentNode
			
		$(elem).toggleClass('mypanel-collapsed').parents('.panel-heading').next().toggleClass('in');
	};
	
	
	//Categories Manager
	$scope.showAddCat = false;
	$scope.catSelected = {};
	$scope.cats = [];
	$scope.loadCat = function(){
		$scope.showAddCat = true;
		$http.get('/admin/categories').success(function(data){
			$scope.cats = data;
			$scope.catSelected = $scope.cats[0];
				
			if($scope.cats && $scope.p.categories){
				$scope.cats.forEach(function(e,i){
					if($scope.p.categories.indexOf(e.name)>-1){
						e.selected = true;
					}
				});
			}
		});
	};
	
	$scope.saveCategories = function(){
		var selectedCats = [];
		
		$scope.cats.forEach(function(e,i){
			if(e.selected){
				selectedCats.push(e.name);
			}
		});

		$scope.p.categories = selectedCats;
		$scope.saveProductGeneric(function(resp){
			if(resp.success){
				$scope.productDetailsForm.$setPristine();
				$scope.showAddCat = false;
			}
			else{
				alert(resp.message)
			}
		});	
		
	};
	
	$scope.saveSimpleProduct = function(){
		
		if($scope.productSimpleForm.$dirty){

			$scope.saveProductGeneric(function(resp){
				if(resp.success){
					$scope.productSimpleForm.$setPristine();
					_lbFns.pSuccess('Prices updated');
				}
				else{
					alert(resp.message)
				}
			});	
		}
	};
	
	//Attribute Manager
	$scope.addAttr = function(){
		
		if($scope.p.attrs==null)
			$scope.p.attrs = [];
		
		$scope.p.attrs.push({
			attr:'New Attr',
			position:0,
			values:[],
			visible:true
		});	
		
		//Set dirty so that the logic to save the form is triggered.
		$scope.productAttrForm.$setDirty();
	};
	
	
	$scope.removeAttr = function(){
		
		var attrToBeRemoved = this.attr.attr;
		angular.forEach($scope.p.attrs, function(attr, i) {
			if(attr.attr == attrToBeRemoved){
				$scope.p.attrs.splice(i,1);
			}		
		});
		
		//Set dirty so that the logic to save the form is triggered.
		$scope.productAttrForm.$setDirty();
	};
	
	$scope.saveAttrs = function(){		
		
		if($scope.productAttrForm.$dirty){			
			$scope.saveProductGeneric(function(resp){
				if(resp.success){
					$scope.toPrices();	
					$scope.productAttrForm.$setPristine();
					$scope.correctVarValues();	
					$scope.getNewVarCombinations();	
					
					/**
					 * If there was a change in Attr
					 * sort #scope.p.attr again 
					 **/
					if($scope.p.attrs){
						//Sort ascending on position
						$scope.p.attrs.sort(function(a,b){
							return (a.position-b.position);
						});
					}
				}
				else{
					$scope.errorMsg = resp.message;
				}
			});			
		}
		else {
			$scope.toPrices();
		}
	};
	
	
	
	$scope.toPrices = function(){	
		$('.product-attr-holder').slideUp();
		$('.product-price-holder').slideDown();
		
		$scope.getDefautAttr();
	};
	

	
	$scope.getPrices = function($event){
		
		$http.get('/admin/products/json/' + $scope.params.productId + '/price')
		.success(function(data){
			$scope.prices = data;
			
		}).error(function(){
			$scope.errorMsg = 'There was some error getting the prices. Please try later';
		});		
	};
	
	
	$scope.setDefautAttr = function(){
		
		var tempDefaultAttr = [];
		if(this.price.variation && this.price.variation.length>0){
			this.price.variation.forEach(function(e,i){				
				tempDefaultAttr.push({'attr':e.attr,'value':e.value});
			});			
			
			$scope.p.defaultAttr = tempDefaultAttr.slice(0);
			
			
			//Save the defaultAttr back into db
			$scope.saveProductGeneric(function(resp){
				if(!resp.success){
					$scope.errorMsg = resp.message;
				}
			});	

			$('.product-default-attr.active').removeClass('active');
			$('.product-default-attr[data-index="' + this.$index + '"]').addClass('active');
		}
		
	};
	
	
	$scope.getDefautAttr = function(){
		
		if(!$scope.p.defaultAttr) return;
		
		for(var i=0; i<$scope.prices.length; i++){

			var tempDefaultAttr = [],
			price = $scope.prices[i];

			price.variation.forEach(function(e,i){				
				tempDefaultAttr.push({'attr':e.attr,'value':e.value});
			});	
			
			if($scope.p.defaultAttr.length==tempDefaultAttr.length 
					&& JSON.stringify($scope.p.defaultAttr)===JSON.stringify(tempDefaultAttr)){
				
				$('.product-default-attr.active').removeClass('active');
				$('.product-default-attr[data-index="' + i + '"]').addClass('active');
				
				break;
			}			
		}
		
	};
	
	
	
	$scope.getNewVarCombinations = function(){
		
		var f = function(curr, obj, array, master){
			var attr = array[curr].attr,
				values = array[curr].values;
			
			curr++;
			
			values.forEach(function(e, i){
				var newObj = obj.slice(0); //Clone the array
				
				newObj.push({'attr':attr, 'value':e});
				
				if(curr<array.length){
					f(curr, newObj, array, master);
				}
				else{
					master.push({'variation':newObj});
				}
				
			});
			
		};
		
		var myMaster = [],
		myObj = [],
		myArray = $scope.p.attrs.slice(0), //clone the array
		myCurr = 0,
		newPrices = [];	
		
		
		/**
		 * Create the new combination array using the recursive function above
		 **/
		f(myCurr, myObj, myArray, myMaster);

		
		
		/**
		 * Create newPrice array based on new combinations
		 */				
		myMaster.forEach(function(e, i){
			var currPrice = {};

			//If there is a priceObject already in the $scope.prices, use it.
			if($scope.prices[i]){
				currPrice = $scope.prices[i];
			}
			
			//Else use the initialized priceObject
			else{
				currPrice = $.extend(true,{},$scope.priceLine);
			}
			
			//Update the variation with the new from the new combination array 
			currPrice.variation = myMaster[i].variation;
			
			newPrices.push(currPrice);
		});
		
		
		/**
		 * Replace the price array with new price array
		 **/
		$scope.prices = newPrices;
		
		//Set dirty so that the logic to save the form is triggered.
		$scope.productPriceForm.$setDirty();
	};
	
	
	$scope.addPriceLine = function(){		
		var currPrice = $.extend(true,{},$scope.priceLine);
		if($scope.prices[0] && $scope.prices[0].variation)
			currPrice.variation = $scope.prices[0].variation;
		
		$scope.prices.push(currPrice);
		
		//Set dirty so that the logic to save the form is triggered.
		$scope.productPriceForm.$setDirty();
	};
	
	
	$scope.removePriceLine = function(){		
		var priceToBeRemoved = this.price,
			keepGoing = true;
		angular.forEach($scope.prices, function(price, i) {
			if(price == priceToBeRemoved && keepGoing){
				$scope.prices.splice(i,1);
				keepGoing = false;
			}		
		});
		
		//Set dirty so that the logic to save the form is triggered.
		$scope.productPriceForm.$setDirty();
	};
	
	
	$scope.savePrices = function(){
		
		if($scope.productPriceForm.$dirty && $scope.productPriceForm.$valid){			
			$http.post('/admin/products/json/savepricedtls', $scope.prices)
			.success(function(resp){
				if(resp.success){
					
					$scope.setPriceRange();
					
					//Set form to pristine, so SAVE button will be disabled.
					$scope.productPriceForm.$setPristine();
					
					_lbFns.pSuccess("Prices Updated!");
					
				}
				else{
					$scope.errorMsg = resp.message;
				}
			})
			.error(function(){
				$scope.errorMsg  = "There was some error saving the price details. Please contact G.";
			});			
		}
		else
			$scope.closeProductDetails();
	};
	
	$scope.setPriceRange = function(){
		var lowerPrice = 999999, higherPrice = -999999;
		
		if($scope.prices.length>1){
			
			$scope.prices.forEach(function(e,i){
				if(e.salePrice && e.salePrice < lowerPrice){
					lowerPrice = e.salePrice;
				}
				
				else if(e.regPrice && e.regPrice <= lowerPrice){
					lowerPrice = e.regPrice;
				}
				

				if(e.salePrice && e.salePrice >= higherPrice){
					higherPrice = e.salePrice;
				}
				
				else if(e.regPrice && e.regPrice >= higherPrice){
					higherPrice = e.regPrice;
				}
				
			});
			
			if(lowerPrice===higherPrice){
				$scope.p.priceRange = $filter('currency')(lowerPrice);
			}
			else if(lowerPrice!=999999 && higherPrice != -999999){
				$scope.p.priceRange = $filter('currency')(lowerPrice) 
						+ ' - ' 
						+ $filter('currency')(higherPrice);
			}
			
		}
		else{
			
			// When there is only one price for this variable product			
			var e = $scope.prices[0];
			if(e.salePrice && e.salePrice < e.regPrice){
				$scope.p.priceRange = $filter('currency')(e.salePrice);
			}
			else{
				$scope.p.priceRange = $filter('currency')(e.regPrice);
			}
			
		}
		
		//Save the price range back into the DB
		$scope.saveProductGeneric(function(resp){});
	};
	
	
	
	$scope.initalize = function(){
		$scope.errorMsg = '';
		$scope.p = {};	
    	$location.path('');	
	};
	
	
	/* MEMO FNS */

	
	
	$scope.memos = [];
	$scope.memopg = {};
	$scope.memopg.currentPage = 1;
	$scope.memoText = "";
	
	$scope.reloadMemos = function(){
		$scope.memopg.currentPage = 1;
		$scope.getMemos();
	};
	
	$scope.getMemos = function(){
		
		$http.get('/admin/logs/products/' + $scope.productId, {
			params : {
				'p' : $scope.memopg.currentPage
			}
		})
		.then(function(resp){
			if(resp.data){
				$scope.memos = resp.data.respData;
				$scope.memopg = resp.data.pg;
			}			
		});		
	};
	$scope.saveMemo = function(){
		var log = {
				collection : 'products',
				key : $scope.productId,
				details : $('textarea#memoText').val(),
				date : new Date(),
				user : $('#adminHeaderUsername').text()
		}
		
		$http.post('/admin/logs/add', log);
		$scope.memos.unshift(log);
		$('textarea#memoText').val('');
	};
	/* MEMO FNS END*/
	
	if($scope.mode=='edit' && $scope.productId){
		$scope.getProductDetails();
		$scope.reloadMemos();
	}
	else {
		
		//Get date after removing the milli seconds
		$scope.newBatchDate = new Date(Math.floor( new Date().getTime() / (1000*60) )*1000*60);
		
		//initialize product obj
		$scope.p = {"_id":0, "status":"private","stockStat":"instock","newBatchArrival":$scope.newBatchDate.getTime()};			
		$('#product-editor').show(0, function(){
			$('.admin-editor-container')[0].scrollIntoView();
			$('[data-toggle="tooltip"]').tooltip();
		});
	}

},

catCtrlrs = function($scope, $http, $filter){
	
	$scope.errorMsg = '';
	$scope.categories = [];
	$scope.categoryId = 0;
	$scope.btnText = 'ADD';
	$scope.catName = '';
	$scope.catDesc = '';
	$scope.catParent = {_id:0, name:'No Parent'}
	$scope.selCats = [];
	$scope.orderAndOrderTxts = _lbConstants.productspage_orderAndOrderTxt; //defined in main.js
	$scope.orderSelect = $scope.orderAndOrderTxts["-newBatchArrival"];
	
	$scope.resetSelCats = function(){		
		$scope.selCats = $scope.categories.slice(0);
		$scope.selCats.unshift($scope.catParent);
	};
	
	$scope.getCategories = function($event){
		
		$http.get('/admin/categories/json/list')
		.success(function(data){
			$scope.categories = data;	
			
			$scope.resetSelCats();
			
			$('#category-editor').show();
			
		}).error(function(){
			$scope.errorMsg = 'There was some error getting the categories. Please try later';		
			$('#category-editor').show();
		});		
	};
	
	
	$scope.getParentCat = function(catId){
		
		var parentName = 'Category Id - ' + catId;
		if($scope.categories){
			for(var i in $scope.categories){
				if($scope.categories[i]._id == catId){
					parentName = $scope.categories[i].name;
					break;
				}
			}
		}
		
		return parentName;
	};
	
	
	$scope.clearForm = function(){

		$scope.categoryId = 0;
		$scope.btnText = 'ADD';
		$scope.catName = '';
		$scope.catDesc = '';
		$scope.catUrl = '';
		$scope.catParent = {_id:0, name:'No Parent'}
		
		$scope.addCategoryForm.$setPristine();
		$scope.addCategoryForm.name.$setPristine();
	};
	
	
	$scope.getParent = function(parentId){
		var cat = $scope.selCats[0];

		if($scope.categories){
			for(var i in $scope.categories){
				if($scope.categories[i]._id == parentId){
					cat = $scope.categories[i];
					break;
				}
			}
		}
		
		return cat;
	};
	
	
	$scope.nameChange = function(){		
		if($scope.catUrl != '')
			$scope.catUrl = $scope.catName.toLowerCase().replace(/\s+/g,"-");
	};
	
	
	$scope.editCat = function(){

		$scope.categoryId = this.c._id;
		$scope.btnText = 'SAVE';
		$scope.catName = this.c.name;
		$scope.catUrl = this.c.url;
		$scope.catDesc = this.c.description;
		$scope.catParent = $scope.getParent(this.c.parent);
		$scope.orderSelect = $scope.orderAndOrderTxts[this.c.sortOrder];
		
		/**
		 * Show subtle color change.
		 **/
		_lbFns.showViewForm();		
	};
	
	
	$scope.removeCat = function(){
		var catId = this.c._id;
		if(confirm('Are you sure you want to delete this?')){
			
			$http.post('/admin/categories/json/delete/' + catId)
			.success(function(resp){
				
				if(resp.success){					
					$scope.categories.forEach(function(e, i){
						if(e._id == catId){
							$scope.categories.splice(i, 1);
						}
					});
				}
				else{
					$scope.errorMsg = resp.message;
				}
				
			}).error(function(){
				$scope.errorMsg = "There was some error deleting category." 
					+ " Please contact G.";
			});
		}
	};
	
	
	$scope.saveCategory = function(){
		
		if($scope.btnText == 'ADD'){
			
			var cat = {'_id':0, 'name': $scope.catName, 'url' : $scope.catUrl};
			
			if($scope.catDesc != '') cat.description = $scope.catDesc;
			if($scope.catParent._id != 0) cat.parent = $scope.catParent._id;
			
			for(var x in $scope.orderAndOrderTxts){
				if($scope.orderAndOrderTxts[x] === $scope.orderSelect){
					cat.sortOrder = x + "";
					break;
				}
			}
			
			$http.post('/admin/categories/json/create', cat)
			.success(function(c){				
				if(c._id == 0){				
					$scope.errorMsg = c.description;
				}
				else{	
					$scope.categories.push(c);
					$scope.resetSelCats();
					$scope.clearForm();
				}
				
			}).error(function(){
				$scope.errorMsg = "There was some error creating category." 
					+ " Please contact G.";
			});
		}
		
		else {
			
			var cat = {'_id':$scope.categoryId, 'name': $scope.catName, 'url' : $scope.catUrl};
			if($scope.catDesc != '') cat.description = $scope.catDesc;
			if($scope.catParent._id != 0) cat.parent = $scope.catParent._id;
			
			for(var x in $scope.orderAndOrderTxts){
				if($scope.orderAndOrderTxts[x] === $scope.orderSelect){
					cat.sortOrder = x + "";
					break;
				}
			}
			
			$http.post('/admin/categories/json/save', cat)
			.success(function(resp){				
				if(resp.success){					
					
					$scope.categories.forEach(function(e, i){
						if(e._id == cat._id){
							$scope.categories.splice(i, 1, cat);							
						}
					});
					
					$scope.resetSelCats();
					$scope.clearForm();
				}
				else{		
					$scope.errorMsg = resp.message;	
				}
				
			}).error(function(){
				$scope.errorMsg = "There was some error saving category." 
					+ " Please contact G.";
			});
			
		}
	};
	
	
	
	$scope.closeCategoryModal = function(){		
		$('#category-editor').hide();
	};
	
	
	$scope.getCategories();
},

reviewCtrlrs = function($scope, $http, $filter){
	
	$scope.errorMsg = '';
	$scope.reviews = [];
	
	$scope.getReviews = function($event){
		
		$http.get('/admin/products/json/list-reviews', {
			params : { 
				'p' : $scope.pg.currentPage,
				's' : $scope.reviewSortSelected.value
			}
		})
		.then(function(resp){
			$scope.pg = resp.data.pg;
			$scope.reviews = resp.data.respData;
			
			$('#review-holder').show();			
		},
		function(){
			$scope.errorMsg = 'There was some error getting the reviews. Please try later';			
			$('#review-holder').show();
		});		
	};	
	
	$scope.pageChanged = function() {
		$scope.getReviews();
	};
	
	
	$scope.updateReview = function(newStatus){
		var currentReview = this.r;
		currentReview.approvalStatus = newStatus;
		
		$http.post('/admin/products/update-review', currentReview)
		.then(function(resp){
			if(resp.data && resp.data.success){				
				_lbFns.pSuccess("Review Updated!");
			}
			else if(resp.data && resp.data.message){
				$scope.errorMsg = resp.data.message;
			}
			else{
				$scope.errorMsg = "There was some error updating review." 
					+ " Please contact G.";
			}
			
		},
		function(){
			$scope.errorMsg = "There was some error updating review." 
				+ " Please contact G.";
		});
	};

	$scope.reviewSortIsOpen = false;
	$scope.reviewSortOptions = [
		{text : 'New reviews', value : 'new'},
		{text : 'Declined reviews', value : 'declined'},
		{text : 'Approved reviews', value : 'approved'},
		{text : 'All reviews', value : ''}
	];
	
	$scope.reviewStat = {approvalStatus : 'new'};
	$scope.reviewSortSelected = $scope.reviewSortOptions[0];
	$scope.toggleDropdown = function($event) {
		$event.preventDefault();
	    $event.stopPropagation();
	    $scope.reviewSortIsOpen = !$scope.reviewSortIsOpen;
	};	
	$scope.changeReviewSort = function(){
		$scope.reviewSortSelected = this.rso;
		$scope.reviewStat = {approvalStatus : $scope.reviewSortSelected.value};
		$scope.getReviews();
	};
	
	
	$scope.closeReviewModal = function(){		
		$('#review-holder').hide();
	};
	
	
	$scope.getReviews();
};

prdApp.config(['$routeProvider',
    function($routeProvider) {
      $routeProvider.
	    when('/details/:productId', {
	         templateUrl: '/resources/ng-templates/admin/product-details.html'+versionCtrl,
	         controller: 'productControllers',
	         resolve : {
	        	 mode:function () {
	        		 return 'edit';
	        	 }
	         }
	    }).
	    
	    when('/addproduct/', {
	         templateUrl: '/resources/ng-templates/admin/product-details.html'+versionCtrl,
	         controller: 'productControllers',
	         resolve : {
	        	 mode:function () {
	        		 return 'new';
	        	 }
	         }
	    }).
	    
	    when('/categories/', {
	         templateUrl: '/resources/ng-templates/admin/category.html'+versionCtrl,
	         controller: 'categoryControllers'
	    }).
	    
	    when('/reviews/', {
	         templateUrl: '/resources/ng-templates/admin/products-reviews.html'+versionCtrl,
	         controller: 'reviewControllers'
	    }).
	    
	    when('', {
	         templateUrl: '/resources/ng-templates/empty.html',
	         controller: 'defaultRouteCtrl'
	    });
}])



//below fns are defined in angular-general-functions.js
.config(appConfigs)
.run(appRunFns) 
//end fns defined in angular-general-functions.js


.controller('defaultRouteCtrl', defaultCtrlr)
.controller('productControllers', prdCtrlrs)
.controller('categoryControllers', catCtrlrs)
.controller('reviewControllers', reviewCtrlrs)

//This is defined in angular-upload-service.js. Common fn for uploads
.controller('uploadCtrlr', uploadCtrlr);

