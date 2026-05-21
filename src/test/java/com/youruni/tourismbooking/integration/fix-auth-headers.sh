#!/bin/bash
# Comprehensive script to add JWT Authorization headers to all integration test files
# Run this in the integration test directory to update all test files

# This script adds .header("Authorization", getBearerTokenHeader()) to all mockMvc.perform calls
# that don't already have it

for file in *IntegrationTest.java; do
    if [ -f "$file" ]; then
        echo "Processing $file..."
        
        # Use sed to add header to standard patterns
        # Pattern: mockMvc.perform(post/get/put/delete(...).contentType(...)
        sed -i 's/mockMvc\.perform(\(post\|get\|put\|delete\)\([^)]*\)\.contentType/mockMvc.perform(\1\2.contentType/g' "$file"
        
        # Pattern: .contentType(...).content(...)) -> add before )
        sed -i 's/\.contentType(\([^)]*\))\.header("Authorization", getBearerTokenHeader())/\.contentType(\1).header("Authorization", getBearerTokenHeader())/g' "$file"
        
        # Add header after contentType if not present
        sed -i 's/\.contentType(MediaType\.[^)]*)\s*$/.contentType(MediaType.APPLICATION_JSON)\n                .header("Authorization", getBearerTokenHeader())/g' "$file"
        
        echo "✓ Updated $file"
    fi
done

echo "All test files updated!"
