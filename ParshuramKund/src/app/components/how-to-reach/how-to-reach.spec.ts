import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HowToReach } from './how-to-reach';

describe('HowToReach', () => {
  let component: HowToReach;
  let fixture: ComponentFixture<HowToReach>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HowToReach],
    }).compileComponents();

    fixture = TestBed.createComponent(HowToReach);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
