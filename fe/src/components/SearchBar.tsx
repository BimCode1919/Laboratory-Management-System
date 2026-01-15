// src/components/SearchBar.tsx

import { Box, IconButton, TextField, Tooltip } from "@mui/material";
import { useState } from "react";
import SearchIcon from "@mui/icons-material/Search";

interface SearchBarProps {
    onSearch: (keyword: string) => void;
    placeholder?: string;
}

export default function SearchBar({
    onSearch,
    placeholder = "Search...",
}: SearchBarProps) {
    const [keyword, setKeyword] = useState("");

    const handleSearch = () => {
        onSearch(keyword.trim());
    };

    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if(e.key === "Enter") handleSearch();
    }

    return (
        <Box display="flex" alignItems="center" gap={1}>
            <TextField
                variant="outlined"
                size="small"
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                onKeyDown={handleKeyDown}
                placeholder={placeholder}
                fullWidth
            />

            <Tooltip title="Search">
                <IconButton color="primary" onClick={handleSearch}>
                    <SearchIcon />
                </IconButton>
            </Tooltip>
        </Box>
    );
}