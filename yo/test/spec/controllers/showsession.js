'use strict';

describe('Controller: ShowsessionCtrl', function () {

  // load the controller's module
  beforeEach(module('sposApp'));

  var ShowsessionCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    ShowsessionCtrl = $controller('ShowsessionCtrl', {
      $scope: scope
      // place here mocked dependencies
    });
  }));

  it('should attach a list of awesomeThings to the scope', function () {
    expect(ShowsessionCtrl.awesomeThings.length).toBe(3);
  });
});
