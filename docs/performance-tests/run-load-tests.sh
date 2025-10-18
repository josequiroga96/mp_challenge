#!/usr/bin/env bash
# Load Testing Suite Runner
# This script provides different pre-configured load test scenarios

set -u

BASE_URL=${BASE_URL:-"http://localhost:8080"}
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_header() {
    echo -e "${BLUE}================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}================================${NC}"
}

print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if application is running
check_application() {
    print_info "Checking if application is running at $BASE_URL"
    if curl -s "$BASE_URL/public/health" > /dev/null 2>&1; then
        print_info "Application is running ✓"
        return 0
    else
        print_error "Application is not running at $BASE_URL"
        print_info "Please start the application first: mvn spring-boot:run"
        return 1
    fi
}

# Quick smoke test
smoke_test() {
    print_header "SMOKE TEST"
    print_info "Running basic functionality test..."
    
    TEST_TYPE="smoke" \
    TEST_PHASES="warmup,read" \
    CODESAMPLE=1 \
    "$SCRIPT_DIR/run-suite-enhanced.sh"
}

# Basic load test
basic_load_test() {
    print_header "BASIC LOAD TEST"
    print_info "Running basic load test with read/write operations..."
    
    TEST_TYPE="basic" \
    TEST_PHASES="warmup,read,write,mixed" \
    CODESAMPLE=1 \
    "$SCRIPT_DIR/run-suite-enhanced.sh"
}

# Concurrency scaling test
scaling_test() {
    print_header "CONCURRENCY SCALING TEST"
    print_info "Testing different concurrency levels..."
    
    TEST_TYPE="scaling" \
    TEST_PHASES="warmup,scaling" \
    CODESAMPLE=1 \
    "$SCRIPT_DIR/run-suite-enhanced.sh"
}

# Spike test
spike_test() {
    print_header "SPIKE TEST"
    print_info "Testing sudden traffic spikes..."
    
    TEST_TYPE="spike" \
    TEST_PHASES="warmup,spikes" \
    CODESAMPLE=1 \
    "$SCRIPT_DIR/run-suite-enhanced.sh"
}

# Stress test
stress_test() {
    print_header "STRESS TEST"
    print_warning "This will push the system to its limits!"
    print_info "Testing system under extreme load..."
    
    TEST_TYPE="stress" \
    TEST_PHASES="warmup,stress" \
    CODESAMPLE=1 \
    "$SCRIPT_DIR/run-suite-enhanced.sh"
}

# Comprehensive test
comprehensive_test() {
    print_header "COMPREHENSIVE TEST"
    print_info "Running all test phases..."
    
    TEST_TYPE="comprehensive" \
    TEST_PHASES="warmup,read,write,mixed,scaling,spikes,stress" \
    CODESAMPLE=1 \
    PERCENTILES=1 \
    "$SCRIPT_DIR/run-suite-enhanced.sh"
}

# Custom test
custom_test() {
    print_header "CUSTOM TEST"
    echo "Available test phases:"
    echo "  - warmup: Basic warmup tests"
    echo "  - read: Read-only operations"
    echo "  - write: Write operations (create, update, patch, delete)"
    echo "  - mixed: Mixed read/write workload"
    echo "  - scaling: Concurrency scaling tests"
    echo "  - spikes: Traffic spike tests"
    echo "  - stress: Stress tests"
    echo ""
    read -p "Enter test phases (comma-separated): " phases
    read -p "Enable code sample analysis? (y/n): " codesample
    read -p "Enable percentiles export? (y/n): " percentiles
    
    local codesample_flag=""
    local percentiles_flag=""
    
    if [[ "$codesample" =~ ^[Yy]$ ]]; then
        codesample_flag="CODESAMPLE=1"
    fi
    
    if [[ "$percentiles" =~ ^[Yy]$ ]]; then
        percentiles_flag="PERCENTILES=1"
    fi
    
    TEST_TYPE="custom" \
    TEST_PHASES="$phases" \
    $codesample_flag \
    $percentiles_flag \
    "$SCRIPT_DIR/run-suite-enhanced.sh"
}

# Show help
show_help() {
    echo "Load Testing Suite Runner"
    echo ""
    echo "Usage: $0 [test_type]"
    echo ""
    echo "Available test types:"
    echo "  smoke        - Quick smoke test (warmup + read)"
    echo "  basic        - Basic load test (warmup + read + write + mixed)"
    echo "  scaling      - Concurrency scaling test"
    echo "  spike        - Traffic spike test"
    echo "  stress       - Stress test (pushes system to limits)"
    echo "  comprehensive - All test phases"
    echo "  custom       - Custom test configuration"
    echo "  help         - Show this help"
    echo ""
    echo "Environment variables:"
    echo "  BASE_URL     - Application URL (default: http://localhost:8080)"
    echo ""
    echo "Examples:"
    echo "  $0 smoke"
    echo "  $0 basic"
    echo "  $0 stress"
    echo "  BASE_URL=http://localhost:8081 $0 comprehensive"
}

# Main execution
main() {
    if [ $# -eq 0 ]; then
        show_help
        exit 1
    fi
    
    local test_type="$1"
    
    # Check if application is running
    if ! check_application; then
        exit 1
    fi
    
    case "$test_type" in
        "smoke")
            smoke_test
            ;;
        "basic")
            basic_load_test
            ;;
        "scaling")
            scaling_test
            ;;
        "spike")
            spike_test
            ;;
        "stress")
            stress_test
            ;;
        "comprehensive")
            comprehensive_test
            ;;
        "custom")
            custom_test
            ;;
        "help"|"-h"|"--help")
            show_help
            ;;
        *)
            print_error "Unknown test type: $test_type"
            show_help
            exit 1
            ;;
    esac
}

# Run main function with all arguments
main "$@"
