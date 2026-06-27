import { Component, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-gallery',
  imports: [CommonModule],
  templateUrl: './gallery.html',
  styleUrl: './gallery.scss',
})
export class Gallery {
  images: string[] = [
    'assets/gallery/19.379a929.jpg',
    'assets/gallery/24.1815634.jpg',
    'assets/gallery/25.dc4ef9c.jpg',
    'assets/gallery/26.79d8b87.jpg',
    'assets/gallery/27.16d0481.jpg',
    'assets/gallery/28.356969e.jpg',
    'assets/gallery/29.ff6516a.jpg',
    'assets/gallery/31.69c38de.jpg',
    'assets/gallery/33.3200d3c.jpg',
    'assets/gallery/34.eeac269.jpg',
    'assets/gallery/35.f78c686.jpg',
    'assets/gallery/36.c4084c9.jpg',
    'assets/gallery/37.44ea031.jpg',
    'assets/gallery/38.bf09e98.jpg',
    'assets/gallery/39.122151b.jpg',
    'assets/gallery/40.3db47b2.jpg',
    'assets/gallery/41.5c8e6db.jpg',
    'assets/gallery/20.2c872bb.jpg',
    'assets/gallery/21.f0e6fa5.jpg',
    'assets/gallery/22.211de7d.jpg',
    'assets/gallery/43.62183f4.jpg',
    'assets/gallery/44.a99aee3.jpg',
    'assets/gallery/23.8a8fac9.jpg',
    'assets/gallery/42.ff878de.jpg',
    'assets/gallery/45.d1b9136.jpg',
    'assets/gallery/46.36aecf6.jpg',
    'assets/gallery/47.24b687b.jpg',
    'assets/gallery/48.eafbd20.jpg',
    'assets/gallery/49.f8d7057.jpg',
    'assets/gallery/50.85e22c7.jpg',
    'assets/gallery/51.20a725b.jpg',
    'assets/gallery/9.ca0347a.jpg',
    'assets/gallery/11.eb4f1bb.jpg',
    'assets/gallery/12.9706faf.jpg',
    'assets/gallery/13.d307fbe.jpg',
    'assets/gallery/14.4f8a26f.jpg',
    'assets/gallery/15.c1faba8.jpg'
  ];

  selectedImageIndex: number | null = null;

  openLightbox(index: number): void {
    this.selectedImageIndex = index;
  }

  closeLightbox(): void {
    this.selectedImageIndex = null;
  }

  nextImage(event?: Event): void {
    if (event) {
      event.stopPropagation();
    }
    if (this.selectedImageIndex !== null) {
      this.selectedImageIndex = (this.selectedImageIndex + 1) % this.images.length;
    }
  }

  prevImage(event?: Event): void {
    if (event) {
      event.stopPropagation();
    }
    if (this.selectedImageIndex !== null) {
      this.selectedImageIndex = (this.selectedImageIndex - 1 + this.images.length) % this.images.length;
    }
  }

  @HostListener('document:keydown', ['$event'])
  handleKeyboardEvent(event: KeyboardEvent): void {
    if (this.selectedImageIndex === null) {
      return;
    }
    if (event.key === 'ArrowRight') {
      this.nextImage();
    } else if (event.key === 'ArrowLeft') {
      this.prevImage();
    } else if (event.key === 'Escape') {
      this.closeLightbox();
    }
  }
}
