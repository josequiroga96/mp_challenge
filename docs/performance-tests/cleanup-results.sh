#!/usr/bin/env bash
# Cleanup script for load test results
# Keeps only the most recent results of each test type

set -u

RESULTS_DIR=${RESULTS_DIR:-"load-test-results"}
KEEP_RECENT=${KEEP_RECENT:-3}  # Keep last 3 results of each type

print_info() {
    echo -e "\033[0;32m[INFO]\033[0m $1"
}

print_warning() {
    echo -e "\033[1;33m[WARNING]\033[0m $1"
}

print_error() {
    echo -e "\033[0;31m[ERROR]\033[0m $1"
}

cleanup_old_results() {
    if [ ! -d "$RESULTS_DIR" ]; then
        print_info "No results directory found at $RESULTS_DIR"
        return 0
    fi
    
    print_info "Cleaning up old results in $RESULTS_DIR"
    print_info "Keeping last $KEEP_RECENT results of each test type"
    
    # Get all test types
    local test_types=$(ls "$RESULTS_DIR" | sed 's/-[0-9]\{8\}-[0-9]\{6\}$//' | sort -u)
    
    for test_type in $test_types; do
        print_info "Processing test type: $test_type"
        
        # Get all directories for this test type, sorted by timestamp (newest first)
        local dirs=$(ls -1t "$RESULTS_DIR" | grep "^$test_type-" | head -n $KEEP_RECENT)
        local all_dirs=$(ls -1t "$RESULTS_DIR" | grep "^$test_type-")
        
        # Count directories
        local keep_count=$(echo "$dirs" | wc -l | tr -d ' ')
        local total_count=$(echo "$all_dirs" | wc -l | tr -d ' ')
        
        if [ "$total_count" -gt "$KEEP_RECENT" ]; then
            local to_remove=$(echo "$all_dirs" | tail -n +$((KEEP_RECENT + 1)))
            
            print_info "  Keeping: $keep_count directories"
            print_info "  Removing: $((total_count - keep_count)) directories"
            
            for dir in $to_remove; do
                print_info "  Removing: $dir"
                rm -rf "$RESULTS_DIR/$dir"
            done
        else
            print_info "  No cleanup needed (only $total_count directories)"
        fi
    done
}

show_usage() {
    echo "Load Test Results Cleanup"
    echo ""
    echo "Usage: $0 [options]"
    echo ""
    echo "Options:"
    echo "  -d, --dir DIR        Results directory (default: load-test-results)"
    echo "  -k, --keep N         Keep last N results of each type (default: 3)"
    echo "  -h, --help           Show this help"
    echo ""
    echo "Examples:"
    echo "  $0                   # Clean up with default settings"
    echo "  $0 -k 5              # Keep last 5 results of each type"
    echo "  $0 -d my-results     # Clean up in custom directory"
}

show_status() {
    if [ ! -d "$RESULTS_DIR" ]; then
        print_info "No results directory found at $RESULTS_DIR"
        return 0
    fi
    
    print_info "Current results in $RESULTS_DIR:"
    echo ""
    
    # Get all test types
    local test_types=$(ls "$RESULTS_DIR" | sed 's/-[0-9]\{8\}-[0-9]\{6\}$//' | sort -u)
    
    for test_type in $test_types; do
        local count=$(ls -1 "$RESULTS_DIR" | grep "^$test_type-" | wc -l | tr -d ' ')
        local latest=$(ls -1t "$RESULTS_DIR" | grep "^$test_type-" | head -n 1)
        echo "  $test_type: $count results (latest: $latest)"
    done
}

main() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            -d|--dir)
                RESULTS_DIR="$2"
                shift 2
                ;;
            -k|--keep)
                KEEP_RECENT="$2"
                shift 2
                ;;
            -h|--help)
                show_usage
                exit 0
                ;;
            status)
                show_status
                exit 0
                ;;
            *)
                print_error "Unknown option: $1"
                show_usage
                exit 1
                ;;
        esac
    done
    
    cleanup_old_results
    print_info "Cleanup completed!"
    show_status
}

main "$@"
