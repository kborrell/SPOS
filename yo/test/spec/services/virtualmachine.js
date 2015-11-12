'use strict';

describe('Service: virtualmachine', function () {

  // load the service's module
  beforeEach(module('yoApp'));

  // instantiate service
  var virtualmachine;
  beforeEach(inject(function (_virtualmachine_) {
    virtualmachine = _virtualmachine_;
  }));

  it('should do something', function () {
    expect(!!virtualmachine).toBe(true);
  });

});
