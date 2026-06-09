import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Auth } from '../../../core/services/auth';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  username = '';
  password = '';
  errorMessage = '';

  constructor(private auth: Auth, private router: Router){}

  onSubmit(): void {
    this.auth.login(this.username, this.password).subscribe({
      next: () => this.router.navigate(['/cards']),
      error: () => this.errorMessage = 'Nieprawidłowy login lub hasło'
    });
  }
}
