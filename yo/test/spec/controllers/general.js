'use strict';

describe('Controller: GeneralCtrl', function () {

  // load the controller's module
  beforeEach(module('sposApp'));

  var GeneralCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    GeneralCtrl = $controller('GeneralCtrl', {
      $scope: scope
      // place here mocked dependencies
    });
  }));

  it('should attach a list of awesomeThings to the scope', function () {
    expect(GeneralCtrl.awesomeThings.length).toBe(3);
  });
});
