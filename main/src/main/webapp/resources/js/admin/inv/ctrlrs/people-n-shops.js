/**
 * 
 */
var addPplCtrlr = function($scope, $http, $filter, $rootScope, $compile, currentUser){
	
	$rootScope.rootPage = "People &amp; Shops";
	$scope.controlId = currentUser.ctrlid;
	
	if($scope.controlId>100) return;
	
	$scope.operators = [];
	$scope.currentOperatorId = 0;
	$scope.opsBtnText = 'ADD';
	
	$http.get('/inventory/apps/listops').success(function(data){
		if(data.success){
			$scope.operators = data.result;
		}
	});
	
	$scope.addOperator=function(){
		if($scope.currentOperatorId!=0){
			
			for(var i=0;i<$scope.operators.length;i++){
				if($scope.operators[i].id==$scope.currentOperatorId){
					$scope.operators[i].operatorName = $scope.operatorName;
					$scope.operators[i].userName = $scope.opsUserName;

					break;
				}
			}
			
			$.post('/inventory/apps/updateops',{id:$scope.currentOperatorId, on:$scope.operatorName, 
						un:$scope.opsUserName, up:$scope.opsPassword, opsid:_luvbriteGlobalOpsId},
			function(data){
				if(!data.success) alert(data.message);
		    });			
			
			$scope.currentOperatorId = 0;
			$scope.operatorName = '';
			$scope.opsUserName = '';
			$scope.opsPassword = "";
			$scope.opsBtnText = 'ADD';
		}
		else {
			$scope.operators.push({operatorName:$scope.operatorName, userName:$scope.opsUserName, 
						dateAdded: $filter('date')(new Date(), 'MM/dd/yyyy hh:mm a')});
			
			$.post('/inventory/apps/newops',{on:$scope.operatorName, un:$scope.opsUserName, 
						up:$scope.opsPassword, opsid:_luvbriteGlobalOpsId},function(data){
				if(data.success){
					for(var i=0;i<$scope.operators.length;i++){
						if($scope.operators[i].operatorName==data.operator.operatorName){
							$scope.operators[i] = data.operator;
							try{$scope.$apply();}catch(err){}
							break;
						}
					}
				}
				else{
					alert(data.message);
				}
		    });
			
			$scope.operatorName = '';	
			$scope.opsUserName = '';
			$scope.opsPassword = "";	
		}
	};
	
	$scope.editOperator=function($event){
		$event.preventDefault();
		
		$scope.operatorName = this.operator.operatorName;
		$scope.opsUserName = this.operator.userName;
		$scope.currentOperatorId = this.operator.id;
		$scope.opsBtnText = 'SAVE';
		
		/**
		 * Scroll up to the form and show subtle color change.
		 **/
		_lbFns.showViewFormS($('#addOperator .view-edit-form'));
	};
	
	$scope.cancelOperatorEdit=function($event){
		$event.preventDefault();
		
		$scope.currentOperatorId = 0;
		$scope.operatorName = '';
		$scope.opsUserName = '';
		$scope.opsPassword = "";
		$scope.opsBtnText = 'ADD';
	};
	
	/****
	 * BOXES
	 ****/

	$scope.boxes = [];
	$scope.currentBoxId = 0;
	$scope.boxBtnText = 'ADD';
	
	$http.get('/inventory/apps/listboxes').success(function(data){
		if(data.success){
			$scope.boxes = data.result;
		}
	});
	
	$scope.addBox=function(){
		if($scope.currentBoxId!=0){
			
			for(var i=0;i<$scope.boxes.length;i++){
				if($scope.boxes[i].id==$scope.currentBoxId){
					$scope.boxes[i].boxName = $scope.boxName;

					break;
				}
			}			
			
			$.post('/inventory/apps/updatebox',{id:$scope.currentBoxId, bn:$scope.boxName, 
				opsid:_luvbriteGlobalOpsId},
			function(data){
				if(!data.success) alert(data.message);
		    });			
			
			$scope.currentBoxId = 0;
			$scope.boxName = '';
			$scope.boxBtnText = 'ADD';
		}
		else {
			$scope.boxes.push({boxName:$scope.boxName});
			
			$.post('/inventory/apps/newbox',{bn:$scope.boxName,
					opsid:_luvbriteGlobalOpsId},function(data){
				if(data.success){
					for(var i=0;i<$scope.boxes.length;i++){
						if($scope.boxes[i].boxName==data.box.boxName){
							$scope.boxes[i] = data.box;
							try{$scope.$apply();}catch(err){}
							break;
						}
					}					
				}
				else{
					alert(data.message);
				}
		    });
			
			$scope.boxName = '';
		}
	};
	
	$scope.editBox=function($event){
		$event.preventDefault();
		
		$scope.boxName = this.box.boxName;
		$scope.currentBoxId = this.box.id;
		$scope.boxBtnText = 'SAVE';
		
		/**
		 * Scroll up to the form and show subtle color change.
		 **/
		_lbFns.showViewFormS($('#addBox .view-edit-form'));
	};
	
	$scope.cancelBoxEdit=function($event){
		$event.preventDefault();
		
		$scope.currentBoxId = 0;
		$scope.boxName = '';
		$scope.boxBtnText = 'ADD';
	};
	
	
	
	
	/****
	 * SHOPS
	 ****/

	$scope.shops = [];
	$scope.currentShopId = 0;
	$scope.shopBtnText = 'ADD';
	
	$http.get('/inventory/apps/listshops').success(function(data){
		if(data.success){
			$scope.shops = data.result;
		}
	});
	
	$scope.addShop=function(){
		if($scope.currentShopId!=0){
			
			for(var i=0;i<$scope.shops.length;i++){
				if($scope.shops[i].id==$scope.currentShopId){
					$scope.shops[i].shopName = $scope.shopName;

					break;
				}
			}			
			
			$.post('/inventory/apps/updateshop',{id:$scope.currentShopId, sn:$scope.shopName, 
				opsid:_luvbriteGlobalOpsId},
			function(data){
				if(!data.success) alert(data.message);
		    });			
			
			$scope.currentShopId = 0;
			$scope.shopName = '';
			$scope.shopBtnText = 'ADD';
		}
		else {
			$scope.shops.push({shopName:$scope.shopName});
			
			$.post('/inventory/apps/newshop',{sn:$scope.shopName,
					opsid:_luvbriteGlobalOpsId},function(data){
				if(data.success){
					for(var i=0;i<$scope.shops.length;i++){
						if($scope.shops[i].shopName==data.shop.shopName){
							$scope.shops[i] = data.shop;
							try{$scope.$apply();}catch(err){}
							break;
						}
					}					
				}
				else{
					alert(data.message);
				}
		    });
			
			$scope.shopName = '';
		}
	};
	
	$scope.editShop=function($event){
		$event.preventDefault();
		
		$scope.shopName = this.shop.shopName;
		$scope.currentShopId = this.shop.id;
		$scope.shopBtnText = 'SAVE';
		
		/**
		 * Scroll up to the form and show subtle color change.
		 **/
		_lbFns.showViewFormS($('#addShop .view-edit-form'));
	};
	
	$scope.cancelShopEdit=function($event){
		$event.preventDefault();
		
		$scope.currentShopId = 0;
		$scope.shopName = '';
		$scope.shopBtnText = 'ADD';
	};
	
	
	
	
	/****
	 * DRIVERS
	 ****/

	$scope.drivers = [];
	$scope.currentDriverId = 0;
	$scope.driverBtnText = 'ADD';
	
	$http.get('/inventory/apps/listdrivers').success(function(data){
		if(data.success){
			$scope.drivers = data.result;
		}
	});
	
	$scope.addDriver=function(){
		if($scope.currentDriverId!=0){
			
			for(var i=0;i<$scope.drivers.length;i++){
				if($scope.drivers[i].id==$scope.currentDriverId){
					$scope.drivers[i].driverName = $scope.driverName;
					$scope.drivers[i].userName = $scope.driverUserName;
					$scope.drivers[i].phoneNumber = $scope.phoneNumber;

					break;
				}
			}			
			
			$.post('/inventory/apps/updatedriver',{id:$scope.currentDriverId, dn:$scope.driverName, un:$scope.driverUserName, 
						up:$scope.driverPassword, dp:$scope.phoneNumber, opsid:_luvbriteGlobalOpsId},
			function(data){
				if(!data.success) alert(data.message);
		    });			
			
			$scope.currentDriverId = 0;
			$scope.driverName = '';
			$scope.driverUserName = '';
			$scope.driverPassword = '';
			$scope.phoneNumber = '';
			$scope.driverBtnText = 'ADD';
		}
		else {
			$scope.drivers.push({driverName:$scope.driverName, userName:$scope.driverUserName, 
				dateAdded: $filter('date')(new Date(), 'MM/dd/yyyy hh:mm a')});
			
			$.post('/inventory/apps/newdriver',{dn:$scope.driverName, un:$scope.driverUserName, dp:$scope.phoneNumber, 
						up:$scope.driverPassword, opsid:_luvbriteGlobalOpsId},function(data){
				if(data.success){
					for(var i=0;i<$scope.drivers.length;i++){
						if($scope.drivers[i].driverName==data.driver.driverName){
							$scope.drivers[i] = data.driver;
							try{$scope.$apply();}catch(err){}
							break;
						}
					}
				}
				else{
					alert(data.message);
				}
		    });
			
			$scope.driverName = '';	
			$scope.driverUserName = '';
			$scope.phoneNumber = '';
			$scope.driverPassword = '';	
		}
	};
	
	$scope.editDriver=function($event){
		$event.preventDefault();
		
		$scope.driverName = this.driver.driverName;
		$scope.driverUserName = this.driver.userName;
		$scope.currentDriverId = this.driver.id;
		$scope.phoneNumber = this.driver.phoneNumber;
		$scope.driverBtnText = 'SAVE';
		
		/**
		 * Scroll up to the form and show subtle color change.
		 **/
		_lbFns.showViewFormS($('#addDriver .view-edit-form'));
	};
	
	$scope.cancelDriverEdit=function($event){
		$event.preventDefault();
		
		$scope.currentDriverId = 0;
		$scope.driverName = '';
		$scope.driverUserName = '';
		$scope.driverPassword = '';
		$scope.phoneNumber = '';
		$scope.driverBtnText = 'ADD';
	};
	
	
	/****
	 * VENDORS
	 ****/	

	$scope.vendors = [];
	$scope.currentVendorId = 0;
	$scope.vendorBtnText = 'ADD';
	
	$http.get('/inventory/apps/listvendors').success(function(data){
		if(data.success){
			$scope.vendors = data.result;
		}
	});
	
	$scope.addVendor=function(){
		
		if($scope.currentVendorId!=0){			
			for(var i=0;i<$scope.vendors.length;i++){
				if($scope.vendors[i].id==$scope.currentVendorId){					
					$scope.vendors[i].vendorName = $scope.vendorName;
					$scope.vendors[i].company = $scope.vendorCompany;
					$scope.vendors[i].address = $scope.vendorAddress;
					$scope.vendors[i].city = $scope.vendorCity;
					$scope.vendors[i].state = $scope.vendorState;
					$scope.vendors[i].zipcode = $scope.vendorZipcode;
					$scope.vendors[i].phone = $scope.vendorPhone;
					$scope.vendors[i].email = $scope.vendorEmail;
					$scope.vendors[i].website = $scope.vendorWebsite;

					break;
				}
			}
			
			$.post('/inventory/apps/updatevendor',{id:$scope.currentVendorId, vn:$scope.vendorName, 
						vc:$scope.vendorCompany, va:$scope.vendorAddress,
						vy:$scope.vendorCity, vs:$scope.vendorState,
						vz:$scope.vendorZipcode, vp:$scope.vendorPhone,
						ve:$scope.vendorEmail, vw:$scope.vendorWebsite,
						opsid:_luvbriteGlobalOpsId},
				function(data){
					if(!data.success) alert(data.message);
				}
			);			
			
			$scope.currentVendorId = 0;
			$scope.vendorName = '';
			$scope.vendorCompany = '';
			$scope.vendorAddress = '';
			$scope.vendorCity = '';
			$scope.vendorState = '';
			$scope.vendorZipcode = '';
			$scope.vendorPhone = '';
			$scope.vendorEmail = '';
			$scope.vendorWebsite = '';
			
			$scope.vendorBtnText = 'ADD';
		}
		else {
			$scope.vendors.push({vendorName:$scope.vendorName, 
						company:$scope.vendorCompany, address:$scope.vendorAddress,
						city:$scope.vendorCity, state:$scope.vendorState,
						zipcode:$scope.vendorZipcode, phone:$scope.vendorPhone,
						email:$scope.vendorEmail, website:$scope.vendorWebsite,
						dateAdded: $filter('date')(new Date(), 'MM/dd/yyyy hh:mm a')});
			
			$.post('/inventory/apps/newvendor',{vn:$scope.vendorName, 
						vc:$scope.vendorCompany, va:$scope.vendorAddress,
						vy:$scope.vendorCity, vs:$scope.vendorState,
						vz:$scope.vendorZipcode, vp:$scope.vendorPhone,
						ve:$scope.vendorEmail, vw:$scope.vendorWebsite,
						opsid:_luvbriteGlobalOpsId},
				function(data){
					if(data.success){
						for(var i=0;i<$scope.vendors.length;i++){
							if($scope.vendors[i].vendorName==data.vendor.vendorName){
								$scope.vendors[i].dateAdded = data.vendor.dateAdded;
								$scope.vendors[i].id = data.vendor.id;
								try{$scope.$apply();}catch(err){}
								break;
							}
						}
					}
					else{
						alert(data.message);
					}
				}
			);
			
			$scope.vendorName = '';
			$scope.vendorCompany = '';
			$scope.vendorAddress = '';
			$scope.vendorCity = '';
			$scope.vendorState = '';
			$scope.vendorZipcode = '';
			$scope.vendorPhone = '';
			$scope.vendorEmail = '';
			$scope.vendorWebsite = '';	
		}
	};
	
	$scope.editVendor=function($event){
		$event.preventDefault();		
		
		$scope.vendorName = this.vendor.vendorName;
		$scope.vendorCompany = this.vendor.company;
		$scope.vendorAddress = this.vendor.address;
		$scope.vendorCity = this.vendor.city;
		$scope.vendorState = this.vendor.state;
		$scope.vendorZipcode = this.vendor.zipcode;
		$scope.vendorPhone = this.vendor.phone;
		$scope.vendorEmail = this.vendor.email;
		$scope.vendorWebsite = this.vendor.website;
		$scope.currentVendorId = this.vendor.id;
		
		$scope.vendorBtnText = 'SAVE';
		
		/**
		 * Scroll up to the form and show subtle color change.
		 **/
		_lbFns.showViewFormS($('#addVendor .view-edit-form'));
	};
	
	$scope.cancelVendorEdit=function($event){
		$event.preventDefault();
		
		$scope.currentVendorId = 0;
		$scope.vendorName = '';
		$scope.vendorCompany = '';
		$scope.vendorAddress = '';
		$scope.vendorCity = '';
		$scope.vendorState = '';
		$scope.vendorZipcode = '';
		$scope.vendorPhone = '';
		$scope.vendorEmail = '';
		$scope.vendorWebsite = '';
		$scope.vendorBtnText = 'ADD';
	};	
};