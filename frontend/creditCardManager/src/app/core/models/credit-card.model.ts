export interface CreditCard{
    id: string;
    cardNumber: string;
    cardLimit: number;
    usedFunds: number;
    status: 'ACTIVE' | 'BLOCKED';
}