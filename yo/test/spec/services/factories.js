'use strict';

describe('Service: factories', function () {

  // load the service's module
  beforeEach(module('sposApp'));

  // instantiate service
  var factories;
  beforeEach(inject(function (_factories_) {
    factories = _factories_;
  }));

  it('should do something', function () {
    expect(!!factories).toBe(true);
  });

});
