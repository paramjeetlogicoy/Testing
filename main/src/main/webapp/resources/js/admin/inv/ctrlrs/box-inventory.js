var addBoxInvCtrlr = function($scope, $http, $routeParams, currentUser){

	$rootScope.rootPage = "Box Inventory";
	$scope.controlId = currentUser.ctrlid;
	
	$scope.currentboxId = 0;
	$scope.boxInvs = [];
	$scope.items = [{value:'in',label:'In the Box'},{value:'sold',label:'Sold'},
	                {value:'',label:'All Types'}];
	$scope.itemSelected = $scope.items[0];
	
	if($routeParams && $routeParams.b){
		$scope.currentboxId = $routeParams.b;
	}	
	
	$scope.boxSelected = {id:0,boxName:'All Boxes'};
	$scope.boxes = [];
	$http.get('/inventory/apps/listboxes').success(function(data){
		if(data.success){
			$scope.boxes = data.result;
			$scope.boxes.unshift($scope.boxSelected);
		}
	});	
	
	$scope.boxChange=function(){
		$scope.currentboxId = $scope.boxSelected.id;
		$scope.getboxInventory();
	};
	
	$scope.getboxInventory=function(){
		var urlParams = '?b='+$scope.currentboxId
				+ '&it='+$scope.itemSelected.value;
		
		$http.get('/inventory/apps/listboxinv'+urlParams).success(function(data){
			if(data.success){
				$scope.boxInvs = data.result;
			}
		});
	};
	
	$scope.getboxInventory();
};