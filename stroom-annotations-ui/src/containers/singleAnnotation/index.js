import { connect } from 'react-redux'
import { withRouter } from 'react-router'

import SingleAnnotation from './singleAnnotation'

import { fetchAnnotation } from '../../actions/fetchAnnotation'
import { createAnnotation } from '../../actions/createAnnotation'

export default connect(
    (state) => ({
        annotation: state.singleAnnotation.annotation
    }),
    {
        createAnnotation,
        fetchAnnotation
    }
)(withRouter(SingleAnnotation));
