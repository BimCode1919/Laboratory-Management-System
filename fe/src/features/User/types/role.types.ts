// role.types.ts
export interface PrivilegeDTO {
  id: number;
  code: string;
  name: string;
}

export interface RoleListDTO {
  id: string;
  code: string;
  name: string;
  description?: string;
  privileges: PrivilegeDTO[];
}

export interface RoleDTO {
  code: string;
  name: string;
  description?: string;
  privilegeIds?: number[];
}

export interface RoleUpdateDTO {
  name: string;
  description?: string;
  privilegeIds?: number[];
}

export interface PrivilegesDTO {
  id: number;
  name: string;
}