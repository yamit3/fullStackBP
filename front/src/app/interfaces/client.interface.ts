export interface Client {
  active: boolean;
  address: string;
  age: number | null;
  gender: string;
  id: number;
  identification: string;
  name: string;
  password?: string;
  phone: string;
}
