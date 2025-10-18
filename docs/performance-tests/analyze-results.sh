#!/usr/bin/env bash
# Results analysis script for load testing
# Provides summary and comparison of different test types

set -u

RESULTS_DIR=${RESULTS_DIR:-"load-test-results"}

print_header() {
    echo -e "\033[0;34m================================\033[0m"
    echo -e "\033[0;34m$1\033[0m"
    echo -e "\033[0;34m================================\033[0m"
}

print_info() {
    echo -e "\033[0;32m[INFO]\033[0m $1"
}

print_warning() {
    echo -e "\033[1;33m[WARNING]\033[0m $1"
}

print_error() {
    echo -e "\033[0;31m[ERROR]\033[0m $1"
}

# Analyze a specific test result
analyze_test() {
    local test_dir="$1"
    local test_name=$(basename "$test_dir" | sed 's/-[0-9]\{8\}-[0-9]\{6\}$//')
    
    if [ ! -f "$test_dir/summary.csv" ]; then
        print_warning "No summary.csv found in $test_dir"
        return 1
    fi
    
    print_header "$test_name Test Results"
    
    # Show basic stats
    echo "Test Directory: $test_dir"
    echo "Total Scenarios: $(tail -n +2 "$test_dir/summary.csv" | wc -l | tr -d ' ')"
    echo ""
    
    # Show throughput summary
    echo "Throughput Summary (requests/sec):"
    tail -n +2 "$test_dir/summary.csv" | awk -F, '{
        if ($6 != "" && $6 != "0") {
            printf "  %-20s: %8.2f req/s\n", $1, $6
        }
    }' | sort -k3 -nr
    echo ""
    
    # Show latency summary
    echo "Latency Summary (P50/P90/P99 ms):"
    tail -n +2 "$test_dir/summary.csv" | awk -F, '{
        if ($7 != "" && $7 != "0") {
            printf "  %-20s: %6.1f/%6.1f/%6.1f ms\n", $1, $7, $8, $9
        }
    }' | sort -k2 -nr
    echo ""
    
    # Show error summary
    echo "Error Summary:"
    tail -n +2 "$test_dir/summary.csv" | awk -F, '{
        if ($4 != "" && $4 != "0") {
            printf "  %-20s: %d failed requests\n", $1, $4
        }
    }'
    echo ""
}

# Compare different test types
compare_tests() {
    print_header "Test Comparison"
    
    if [ ! -d "$RESULTS_DIR" ]; then
        print_error "No results directory found at $RESULTS_DIR"
        return 1
    fi
    
    # Get all test directories, sorted by timestamp
    local test_dirs=$(ls -1t "$RESULTS_DIR" | grep -E '^[a-z]+-[0-9]{8}-[0-9]{6}$')
    
    if [ -z "$test_dirs" ]; then
        print_error "No test results found in $RESULTS_DIR"
        return 1
    fi
    
    echo "Available test results:"
    for dir in $test_dirs; do
        local test_name=$(echo "$dir" | sed 's/-[0-9]\{8\}-[0-9]\{6\}$//')
        local timestamp=$(echo "$dir" | sed 's/^[a-z]*-//')
        echo "  - $test_name ($timestamp)"
    done
    echo ""
    
    # Show throughput comparison
    echo "Throughput Comparison (requests/sec):"
    echo "Test Type           | Max Throughput | Scenario"
    echo "--------------------|----------------|-------------------"
    
    for dir in $test_dirs; do
        local test_name=$(echo "$dir" | sed 's/-[0-9]\{8\}-[0-9]\{6\}$//')
        local summary_file="$RESULTS_DIR/$dir/summary.csv"
        
        if [ -f "$summary_file" ]; then
            local max_throughput=$(tail -n +2 "$summary_file" | awk -F, '{
                if ($6 != "" && $6 != "0") {
                    print $6
                }
            }' | sort -nr | head -n 1)
            
            local max_scenario=$(tail -n +2 "$summary_file" | awk -F, -v max="$max_throughput" '{
                if ($6 == max) {
                    print $1
                    exit
                }
            }')
            
            if [ -n "$max_throughput" ]; then
                printf "%-20s | %14.2f | %s\n" "$test_name" "$max_throughput" "$max_scenario"
            fi
        fi
    done
    echo ""
    
    # Show latency comparison
    echo "Latency Comparison (P99 ms):"
    echo "Test Type           | Max P99 Latency | Scenario"
    echo "--------------------|-----------------|-------------------"
    
    for dir in $test_dirs; do
        local test_name=$(echo "$dir" | sed 's/-[0-9]\{8\}-[0-9]\{6\}$//')
        local summary_file="$RESULTS_DIR/$dir/summary.csv"
        
        if [ -f "$summary_file" ]; then
            local max_p99=$(tail -n +2 "$summary_file" | awk -F, '{
                if ($9 != "" && $9 != "0") {
                    print $9
                }
            }' | sort -nr | head -n 1)
            
            local max_scenario=$(tail -n +2 "$summary_file" | awk -F, -v max="$max_p99" '{
                if ($9 == max) {
                    print $1
                    exit
                }
            }')
            
            if [ -n "$max_p99" ]; then
                printf "%-20s | %15.1f | %s\n" "$test_name" "$max_p99" "$max_scenario"
            fi
        fi
    done
    echo ""
}

# Show detailed scenario analysis
analyze_scenarios() {
    local test_dir="$1"
    
    if [ ! -f "$test_dir/summary.csv" ]; then
        print_error "No summary.csv found in $test_dir"
        return 1
    fi
    
    print_header "Detailed Scenario Analysis"
    
    echo "Scenario              | Concurrency | Requests | Failed | Non-2xx | RPS    | P50ms | P90ms | P99ms"
    echo "----------------------|-------------|----------|--------|---------|--------|-------|-------|-------"
    
    tail -n +2 "$test_dir/summary.csv" | awk -F, '{
        printf "%-21s | %11s | %8s | %6s | %7s | %6s | %5s | %5s | %5s\n", 
               $1, $2, $3, $4, $5, $6, $7, $8, $9
    }'
    echo ""
}

# Show system limits analysis
analyze_limits() {
    print_header "System Limits Analysis"
    
    if [ ! -d "$RESULTS_DIR" ]; then
        print_error "No results directory found at $RESULTS_DIR"
        return 1
    fi
    
    # Find stress test results
    local stress_dir=$(ls -1t "$RESULTS_DIR" | grep "^stress-" | head -n 1)
    
    if [ -z "$stress_dir" ]; then
        print_warning "No stress test results found"
        return 1
    fi
    
    local summary_file="$RESULTS_DIR/$stress_dir/summary.csv"
    
    if [ ! -f "$summary_file" ]; then
        print_error "No summary.csv found in stress test results"
        return 1
    fi
    
    echo "Stress Test Results:"
    echo "Scenario              | Concurrency | Requests | Failed | RPS    | P99ms"
    echo "----------------------|-------------|----------|--------|--------|-------"
    
    tail -n +2 "$summary_file" | awk -F, '{
        if ($2 != "" && $2 != "0") {
            printf "%-21s | %11s | %8s | %6s | %6s | %5s\n", 
                   $1, $2, $3, $4, $6, $9
        }
    }'
    echo ""
    
    # Calculate failure rate
    local total_requests=$(tail -n +2 "$summary_file" | awk -F, '{sum += $3} END {print sum}')
    local total_failed=$(tail -n +2 "$summary_file" | awk -F, '{sum += $4} END {print sum}')
    local total_non2xx=$(tail -n +2 "$summary_file" | awk -F, '{sum += $5} END {print sum}')
    
    if [ "$total_requests" -gt 0 ]; then
        local failure_rate=$(echo "scale=2; $total_failed * 100 / $total_requests" | bc -l 2>/dev/null || echo "0")
        local non2xx_rate=$(echo "scale=2; $total_non2xx * 100 / $total_requests" | bc -l 2>/dev/null || echo "0")
        
        echo "Overall Statistics:"
        echo "  Total Requests: $total_requests"
        echo "  Failed Requests: $total_failed ($failure_rate%)"
        echo "  Non-2xx Responses: $total_non2xx ($non2xx_rate%)"
        echo ""
        
        if [ "$failure_rate" -gt 5 ]; then
            print_warning "High failure rate detected: $failure_rate%"
        fi
        
        if [ "$non2xx_rate" -gt 10 ]; then
            print_warning "High non-2xx response rate: $non2xx_rate%"
        fi
    fi
}

# Main function
main() {
    local command="${1:-compare}"
    
    case "$command" in
        "compare")
            compare_tests
            ;;
        "analyze")
            if [ $# -lt 2 ]; then
                print_error "Usage: $0 analyze <test_directory>"
                return 1
            fi
            analyze_test "$2"
            ;;
        "scenarios")
            if [ $# -lt 2 ]; then
                print_error "Usage: $0 scenarios <test_directory>"
                return 1
            fi
            analyze_scenarios "$2"
            ;;
        "limits")
            analyze_limits
            ;;
        "all")
            compare_tests
            echo ""
            analyze_limits
            ;;
        *)
            echo "Load Test Results Analyzer"
            echo ""
            echo "Usage: $0 [command] [options]"
            echo ""
            echo "Commands:"
            echo "  compare              - Compare all test results (default)"
            echo "  analyze <dir>        - Analyze specific test directory"
            echo "  scenarios <dir>      - Show detailed scenario breakdown"
            echo "  limits               - Analyze system limits from stress tests"
            echo "  all                  - Run all analyses"
            echo ""
            echo "Examples:"
            echo "  $0 compare"
            echo "  $0 analyze load-test-results/stress-20251018-022356"
            echo "  $0 scenarios load-test-results/scaling-20251018-022328"
            echo "  $0 limits"
            ;;
    esac
}

main "$@"
