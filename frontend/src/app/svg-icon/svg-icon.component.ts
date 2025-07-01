import { HttpClient } from '@angular/common/http';
import { Component, Input } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { catchError, map, of } from 'rxjs';

@Component({
  selector: 'app-svg-icon',
  imports: [],
  templateUrl: './svg-icon.component.html',
  styleUrl: './svg-icon.component.css'
})
export class SvgIconComponent {
  @Input() src = '';
  svgContent: SafeHtml | null = null;

  constructor(private sanitizer: DomSanitizer, private http: HttpClient) {}

  ngOnChanges() {
    if (this.src) {
      this.http.get(this.src, { responseType: 'text' })
      .pipe(
        map((svg) => this.sanitizer.bypassSecurityTrustHtml(svg)),
        catchError(() => of('Error while loading the svg.'))
      )
      .subscribe((svg) => this.svgContent = svg);
    }
  }
}
