import { connect } from 'react-redux'

import { createAnnotation } from '../../actions/createAnnotation';

import CreateAnnotation from './createAnnotation'

export default connect(
    null,
    {
        createAnnotation
    }
)(CreateAnnotation)
