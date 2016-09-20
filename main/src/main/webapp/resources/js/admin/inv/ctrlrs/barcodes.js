var barCodeCtrlr = function($scope, $http, $rootScope, currentUser){

	$rootScope.rootPage = "Bar Codes";
	$scope.controlId = currentUser.ctrlid;
	
	if($scope.controlId>100) return;	
	
	$scope.noOfSheets = 1;
	$scope.startingNumber = 0;
	$scope.barcodePrefix = "";
	$scope.skip = 0;	

	$scope.products = [];
	$scope.prodSelected = {id:0, productName:'Select Product'};
	$http.get('/inventory/apps/listallprods').success(function(data){
		if(data.success){
			$scope.products = data.result;
			$scope.products.unshift($scope.prodSelected);
		}
	});
	
	$scope.getNextValue=function(){
		$http.get('/inventory/apps/listbcis').success(function(data){
			if(data.success){
				$scope.startingNumber = data.result[0].nextValue;
				$scope.barcodePrefix = data.result[0].barcodePrefix;
			}
		});
	};	
	$scope.getNextValue();
	
	$scope.generatePDF = function() {	
		window.location = '/inventory/apps/getbarcode?sn='+$scope.startingNumber+'&ns='+$scope.noOfSheets+'&opsid='+_luvbriteGlobalOpsId;
	};
	
	$scope.prodList = [];
	$scope.addObjs = function(){
		$scope.prodList.push($scope.prodSelected);
	};
	
	$scope.removeObj = function(){
		
		var currProd = this.item;
		for(var i=0;i<$scope.prodList.length;i++){
			
			if($scope.prodList[i]==currProd){				
				$scope.prodList.splice(i,1);
				
				break;
			}
		}
	};
	
	$scope.generateAllPrdBarcodes = function(){		
		window.location = '/inventory/apps/getproductbarcode?skip=' + $scope.skip;
	};
	
	$scope.generatePrdBarcodes = function(){
		var objs = [];
		$scope.prodList.forEach(function(prod, index){
			objs.push(prod.id + '~' + prod.productName);
		});
		
		window.location = '/inventory/apps/getproductbarcode?skip=' + $scope.skip + '&objs='+objs.join();
	};	
};