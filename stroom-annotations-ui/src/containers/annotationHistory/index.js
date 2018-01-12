import { connect } from 'react-redux'
import { withRouter } from 'react-router'

import AnnotationHistory from './annotationHistory'

import { fetchAnnotationHistory } from '../../actions/fetchAnnotationHistory'

export default connect(
    (state) => ({
        annotationHistory: state.history
    }),
    {
        fetchAnnotationHistory
    }
)(withRouter(AnnotationHistory));
