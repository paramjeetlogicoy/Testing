var ModalAssignDrvInstanceCtrlr = function ($scope, $uibModalInstance, drivers, order){

	  $scope.drivers = drivers;	  
	  $scope.driverSelected = $scope.drivers[0];	  
	  $scope.order = order;

	  $scope.ok = function () {
		  $scope.order.dispatch.driverName = $scope.driverSelected.driverName;
		  
		  var sendBack = {order:$scope.order, driverId:$scope.driverSelected.id};
		  $uibModalInstance.close(sendBack);
	  };

	  $scope.cancel = function () {
		  $uibModalInstance.dismiss('cancel');
	  };
};