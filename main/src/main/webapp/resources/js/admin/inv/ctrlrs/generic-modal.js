var ModalGenericCtrlr = function ($scope, $uibModalInstance, ms){

	$scope.ms = ms;
	
	$scope.ok = function () {
		$uibModalInstance.close($scope.ms);
	};

	$scope.cancel = function () {
		$uibModalInstance.dismiss('cancel');
	};
};