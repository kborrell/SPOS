'use strict';

describe('Service: modelinfo', function () {

  // load the service's module
  beforeEach(module('sposApp'));

  // instantiate service
  var modelinfo;
  beforeEach(inject(function (_modelinfo_) {
    modelinfo = _modelinfo_;
  }));

  it('should do something', function () {
    expect(!!modelinfo).toBe(true);
  });

});
