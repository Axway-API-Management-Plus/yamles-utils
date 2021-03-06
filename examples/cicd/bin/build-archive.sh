#!/bin/bash
#======================================================================
#== Example script to configure a stage specific API Manager
#======================================================================
set -euo pipefail

CMD_NAME=$(basename $0)
CMD_HOME=$(dirname $0)

helpAndExit() {
  echo "${CMD_NAME}  -  build and configure YAMLES archive"
  echo ""
  echo "Usage:"
  echo "  ${CMD_NAME} -e ENV [--debug]"
  echo ""
  echo "Options:"
  echo "  -e ENV"
  echo "      Target environment (local, test or prod)."
  echo ""
  echo "  --debug"
  echo "      Enable debug messages"
  echo ""  
  echo "  Configure API Manager for target environment and build"
  echo "  deployment archive."
  echo ""
  exit 1
}

parseArgs() {
  DEBUG=0
  while [[ $# -gt 0 ]]
  do
    option="$1"
    case $option in
      -e)
        if [[ $# -gt 1 ]]
        then
          OPT_ENV="$2"
          shift 2
          case $OPT_ENV in
            local)
              ;;
            test)
              ;;
            prod)
              ;;
            *)
              echo "ERROR: unknown environment"
              exit 1
          esac
        else
          echo "ERROR: missing environment"
          exit 1
        fi
        ;;

      --debug)
        DEBUG=1
        shift
        ;;

      *)
        echo "ERROR: unknown option $1"
        exit 1
    esac
  done
}

checkArgs() {
  if [ -z "${OPT_ENV:-}" ]
  then
    echo "ERROR: missing -e option"
    exit 1
  fi
}

# Parse command line
if [ $# -lt 1 ]
then
  helpAndExit
fi

parseArgs $*
checkArgs

# Check prerequistes
if [ -z "${AXWAY_HOME:-}" ]
then
  echo "ERROR: AXWAY_HOME not set"
  exit 1
fi
if [ ! -d "${AXWAY_HOME}/apigateway" ]
then
  echo "ERROR: invalid AXWAY_HOME directory: ${AXWAY_HOME}"
  exit 1
fi

# Define variables
PRJ_HOME="$(realpath ${CMD_HOME}/../../..)"
PRJ_SRC="${PRJ_HOME}/examples/apim"

CFG_DIR="$(realpath ${CMD_HOME}/../config)"
RULES_DIR="$(realpath ${CMD_HOME}/../rules)"
LOOKUP_DIR="$(realpath ${CMD_HOME}/../lookup)"

TARGET_DIR="${PRJ_HOME}/target"
TARGET_TMP_DIR="${TARGET_DIR}/tmp"
TARGET_ARCHIVE="${TARGET_DIR}/apim.tar.gz"

# Set tools
YAMLES="${AXWAY_HOME}/apigateway/posix/bin/yamles"
if [ ! -f "${YAMLES}" ]
then
  YAMLES="${AXWAY_HOME}/apigateway/Win32/bin/yamles.bat"
fi
for j in "${TARGET_DIR}/yamles-utils-*-jar-with-dependencies.jar"
do
  JAR="$j"
  break;
done
YAMLES_UTILS="java -jar ${JAR}"
if [ ${DEBUG} -gt 0 ]
then
  YAMLES_UTILS="${YAMLES_UTILS} -v"
fi


# Prepare for configuration
echo ""
echo "============================================================"
echo "== Prepare for configuration"
echo "============================================================"
if [ -d "${TARGET_TMP_DIR}" ]
then
  rm -r "${TARGET_TMP_DIR}"
fi
TMP_PRJ="${TARGET_TMP_DIR}/apim"

mkdir -p "${TMP_PRJ}"
cp -rt "${TMP_PRJ}" "${PRJ_SRC}/."
echo "INFO : temp. project created ${TMP_PRJ}"


# Lint project
echo ""
echo "============================================================"
echo "== Lint project"
echo "============================================================"
args=("lint" "--project=${TMP_PRJ}" \
      "-r" "${RULES_DIR}/cassandra.rules.yaml" \
      "-r" "${RULES_DIR}/db.rules.yaml" \
     )
${YAMLES_UTILS} "${args[@]}"
echo "INFO : project rules checked"

# Configure project
echo ""
echo "============================================================"
echo "== Configure project - certificates"
echo "============================================================"
args=("merge" "certs" "--project=${TMP_PRJ}")
case $OPT_ENV in
  local)
    args+=( \
      # Configure certificates for local development environment
      "--config=${CFG_DIR}/devs/local/certificates.yaml" \

      # Use local lookups for passphrases
      "--lookup-yaml=${LOOKUP_DIR}/devs/local/lookup.yaml" \
    )
    ;;
  test)
    args+=( \
      # Configure test certificates provided by operators
      "--config=${CFG_DIR}/ops/test/certificates.yaml" \

      # Use KeePass DB from operators to lookup passphrases
      "--kdb=${LOOKUP_DIR}/ops/all/ops-secrets.kdbx" "--kdb-pass=changeme" \
    )
    ;;
  prod)
    args+=( \
      # Configure production certificates provided by operators
      "--config=${CFG_DIR}/ops/prod/certificates.yaml" \

      # Use KeePass DB from operators to lookup passphrases
      "--kdb=${LOOKUP_DIR}/ops/all/ops-secrets.kdbx" "--kdb-pass=changeme" \
    )
    ;;
  *)
    echo "ERROR: unsupported environment"
    exit 1
esac
${YAMLES_UTILS} "${args[@]}"
echo "INFO : certificates generated"

echo ""
echo "============================================================"
echo "== Configure project - values"
echo "============================================================"
args=("merge" "config" "--project=${TMP_PRJ}")
case $OPT_ENV in
  local)
    args+=( \
      # Configure general settings
      "--config=${CFG_DIR}/devs/local/values.yaml" \

      # Configure values for which only developers are responsible for
      # in a separate file.
      "--config=${CFG_DIR}/devs/local/devs-values.yaml" \

      # Use YAML file to lookup configurations and secrets
      "--lookup-yaml=${LOOKUP_DIR}/devs/local/lookup.yaml" \
    )
    ;;
  test)
    args+=( \
      # Read values for which only developers are responsible for
      "--config=${CFG_DIR}/devs/test/devs-values.yaml" \

      # Read values for which operators are responsible for
      # (must be after the developers to configuration to prevent overwrite)
      "--config=${CFG_DIR}/ops/all/values.yaml" \
      "--config=${CFG_DIR}/ops/test/values.yaml" \

      # Use developers KeePass DB for values maintained by developers
      "--kdb=${LOOKUP_DIR}/devs/all/devs-secrets.kdbx" "--kdb-pass=changeme-devs" \

      # Use operators KeePass DB for values maintained by operators
      # (must be after the developers to configuration to prevent overwrite)      
      "--kdb=${LOOKUP_DIR}/ops/all/ops-secrets.kdbx" "--kdb-pass=changeme" \

      # Use operators maintained YAML based lookup file
      "--lookup-yaml=${LOOKUP_DIR}/ops/test/lookup.yaml" \
    )
    ;;
  prod)
    args+=( \
      # Read values for which only developers are responsible for    
      "--config=${CFG_DIR}/devs/prod/devs-values.yaml" \

      # Read values for which operators are responsible for
      # (must be after the developers to configuration to prevent overwrite)
      "--config=${CFG_DIR}/ops/all/values.yaml" \
      "--config=${CFG_DIR}/ops/prod/values.yaml" \

      # Use developers KeePass DB for values maintained by developers
      "--kdb=${LOOKUP_DIR}/devs/all/devs-secrets.kdbx" "--kdb-pass=changeme-devs" \

      # Use operators KeePass DB for values maintained by operators
      # (must be after the developers to configuration to prevent overwrite)      
      "--kdb=${LOOKUP_DIR}/ops/all/ops-secrets.kdbx" "--kdb-pass=changeme" \

      # Use operators maintained YAML based lookup file
      "--lookup-yaml=${LOOKUP_DIR}/ops/prod/lookup.yaml" \
    )
    ;;
  *)
    echo "ERROR: unsupported environment"
    exit 1
esac
${YAMLES_UTILS} "${args[@]}"


# Build archive
echo ""
echo "============================================================"
echo "== Build archive"
echo "============================================================"
rm "${TMP_PRJ}/META-INF/manifest.mf"
tar -C "${TMP_PRJ}" -zcf ${TARGET_ARCHIVE} "."

# Validate archive
echo ""
echo "============================================================"
echo "== Validate configured project"
echo "============================================================"
${YAMLES} validate -s "${TARGET_ARCHIVE}"

echo ""
echo "============================================================"
echo "== Finished"
echo "============================================================"
echo ""
echo "Use the following command to deploy the configured archive to"
echo "the API Gateway."
echo ""
echo "  ${AXWAY_HOME}/apigateway/posix/bin/managedomain --deploy \\"
echo "    --archive_filename ${TARGET_ARCHIVE} \\"
echo "    --group APIM \\"
echo "    --username admin --password changeme"
