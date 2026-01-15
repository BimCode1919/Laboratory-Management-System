// src/utils/navigation.ts

export let navigateTo = (path: string) => {
    console.warn(`Attempted to navigate to ${path}, but navigateTo has not been initialized.`);
};

export const setNavigator = (navigator: (path: string) => void) => {
    navigateTo = navigator;
};