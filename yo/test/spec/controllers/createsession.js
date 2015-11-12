'use strict';

describe('Controller: CreatesessionCtrl', function () {

  // load the controller's module
  beforeEach(module('yoApp'));

  var CreatesessionCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    CreatesessionCtrl = $controller('CreatesessionCtrl', {
      $scope: scope
      // place here mocked dependencies
    });
  }));

  it('should attach a list of awesomeThings to the scope', function () {
    expect(CreatesessionCtrl.awesomeThings.length).toBe(3);
  });
});
