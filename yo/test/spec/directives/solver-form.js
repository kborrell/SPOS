'use strict';

describe('Directive: solverForm', function () {

  // load the directive's module
  beforeEach(module('sposApp'));

  var element,
    scope;

  beforeEach(inject(function ($rootScope) {
    scope = $rootScope.$new();
  }));

  it('should make hidden element visible', inject(function ($compile) {
    element = angular.element('<solver-form></solver-form>');
    element = $compile(element)(scope);
    expect(element.text()).toBe('this is the solverForm directive');
  }));
});
