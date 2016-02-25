'use strict';

describe('Directive: problemForm', function () {

  // load the directive's module
  beforeEach(module('sposApp'));

  var element,
    scope;

  beforeEach(inject(function ($rootScope) {
    scope = $rootScope.$new();
  }));

  it('should make hidden element visible', inject(function ($compile) {
    element = angular.element('<problem-form></problem-form>');
    element = $compile(element)(scope);
    expect(element.text()).toBe('this is the problemForm directive');
  }));
});
