import { Component, OnInit } from '@angular/core';
import { MaterialModule } from '../../material.module';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-how-to-reach',
  imports: [MaterialModule, CommonModule],
  templateUrl: './how-to-reach.html',
  styleUrl: './how-to-reach.scss',
})
export class HowToReach implements OnInit {
  activeTab: 'air' | 'train' | 'road' = 'air';

  // Control lazy loading of maps after initial view render
  mapInitialized = {
    air: false,
    train: false,
    road: false,
  };

  // Track if map has completed loading inside iframe
  mapLoaded = {
    air: false,
    train: false,
    road: false,
  };

  ngOnInit() {
    // Delay loading the active map by 1.2 seconds to allow the page images,
    // layout, navigation, and fonts to load and render with 100% bandwidth.
    setTimeout(() => {
      this.mapInitialized.air = true;
    }, 1200);
  }

  selectTab(tab: 'air' | 'train' | 'road') {
    this.activeTab = tab;
    // Load the map immediately on tab click if not already loaded
    if (!this.mapInitialized[tab]) {
      this.mapInitialized[tab] = true;
    }
  }

  onMapLoad(tab: 'air' | 'train' | 'road') {
    this.mapLoaded[tab] = true;
  }
}

