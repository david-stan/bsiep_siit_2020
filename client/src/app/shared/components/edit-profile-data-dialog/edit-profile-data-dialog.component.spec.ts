import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EditProfileDataDialogComponent } from './edit-profile-data-dialog.component';

describe('EditProfileDataDialogComponent', () => {
  let component: EditProfileDataDialogComponent;
  let fixture: ComponentFixture<EditProfileDataDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EditProfileDataDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EditProfileDataDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
