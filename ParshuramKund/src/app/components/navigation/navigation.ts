import { Component } from '@angular/core';
import { MaterialModule } from '../../material.module';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-navigation',
  imports: [CommonModule, RouterModule, MaterialModule],
  templateUrl: './navigation.html',
  styleUrl: './navigation.scss',
})
export class Navigation {}
