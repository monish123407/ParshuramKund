import { Component, OnDestroy, OnInit } from '@angular/core';
import { MaterialModule } from '../../material.module';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-home',
  imports: [
    MaterialModule,
    CommonModule
  ],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class Home implements OnInit, OnDestroy {
  noPause = false;
  currentIndex = 0;
  intervalId: any;

  slides = [
    {
      image: '../../assets/2.jpeg',
      tagline: 'From 12th - 16th JAN 2027',
      title: 'Parshuram Kund Mela - 2027',
      subtitle: 'The Kumbh of Northeast',
      description: 'Visit the sacred shrine of Sage Parshuram and experience the spiritual tranquility.'
    },
    {
      image: '../../assets/3.jpg',
      tagline: 'Holy Dip in Lohit River',
      title: 'Spiritual Journey of Devotion',
      subtitle: 'Atonement and Sacred Bath',
      description: 'Experience peace, devotion, and divinity amidst the beauty of Arunachal Pradesh.'
    },
    {
      image: '../../assets/1.png',
      tagline: 'Welcome Devotees & Pilgrims',
      title: 'Explore Parshuram Kund',
      subtitle: 'Cultural Heritage & Vibe',
      description: 'Discover rich local culture, historical significance, and breathtaking landscapes.'
    }
  ];

  ngOnInit(): void {
    this.autoSlide();
  }

  autoSlide(): void {
    this.intervalId = setInterval(() => {
      this.nextSlideQuiet();
    }, 5000); // 5 seconds interval for smoother reading
  }

  nextSlideQuiet(): void {
    if (this.currentIndex < this.slides.length - 1) {
      this.currentIndex++;
    } else {
      this.currentIndex = 0;
    }
  }

  prevSlide(): void {
    this.resetTimer();
    if (this.currentIndex > 0) {
      this.currentIndex--;
    } else {
      this.currentIndex = this.slides.length - 1;
    }
  }

  nextSlide(): void {
    this.resetTimer();
    this.nextSlideQuiet();
  }

  setSlide(idx: number): void {
    this.resetTimer();
    this.currentIndex = idx;
  }

  resetTimer(): void {
    if (this.intervalId) {
      clearInterval(this.intervalId);
      this.autoSlide();
    }
  }

  ngOnDestroy(): void {
    if (this.intervalId) {
      clearInterval(this.intervalId);
    }
  }
}
