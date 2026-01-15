export type CreateConfigurationRequest = {
  configName: string;
  configKey: string;
  configValue: string;
  defaultValue?: string;
  description?: string;
  global: boolean;
  instrumentId?: string;
};

export type ConfigurationDTO = {
  id: string;
  configName: string;
  configKey: string;
  configValue: string;
  defaultValue?: string;
  description?: string;
  global: boolean;
  instrumentId?: string;
};
