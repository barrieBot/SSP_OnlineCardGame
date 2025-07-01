import { TestBed } from '@angular/core/testing';

import { GamestateService } from './gamestate.service';

describe('GamestateService', () => {
  let service: GamestateService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(GamestateService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
