import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { YearInputDialogComponent } from './year-input-dialog.component';

describe('YearInputDialogComponent', () => {
  let component: YearInputDialogComponent;
  let fixture: ComponentFixture<YearInputDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ YearInputDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(YearInputDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
